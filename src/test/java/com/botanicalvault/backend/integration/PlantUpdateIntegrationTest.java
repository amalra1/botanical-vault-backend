package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.bearerToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlantUpdateIntegrationTest {
    private static final String JERIVA =
            "{\"commonName\":\"Jerivá\",\"typeId\":4,\"sizeId\":3,\"images\":[{\"viewId\":1},{\"viewId\":2}]}";
    private static final String UPDATE_TEMPLATE =
            "{\"commonName\":\"Jerivá-anão\",\"typeId\":4,\"sizeId\":2,\"originIds\":[2],"
                    + "\"images\":[{\"viewId\":3},{\"id\":%d,\"viewId\":1}]}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void replacesImagesAndRemovesOldFiles() throws Exception {
        String token = bearerToken(mockMvc);
        String created = createJeriva(token);
        Integer id = JsonPath.read(created, "$.id");
        Integer topImageId = JsonPath.read(created, "$.images[1].id");
        String removedUrl = JsonPath.read(created, "$.images[0].url");

        mockMvc.perform(multipart(HttpMethod.PUT, "/plants/" + id)
                        .file(json(UPDATE_TEMPLATE.formatted(topImageId)))
                        .file(png("jeriva-perspectiva.png"))
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commonName").value("Jerivá-anão"))
                .andExpect(jsonPath("$.size.name").value("Médio"))
                .andExpect(jsonPath("$.origins[0].name").value("Mata Atlântica"))
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.images[0].originalFilename").value("jeriva-perspectiva.png"))
                .andExpect(jsonPath("$.images[0].id").isNumber())
                .andExpect(jsonPath("$.images[1].id").value(topImageId))
                .andExpect(jsonPath("$.images[1].view.name").value("Elevação"));

        mockMvc.perform(get(URI.create(removedUrl))).andExpect(status().isNotFound());
    }

    @Test
    void deletesPlantAndItsFiles() throws Exception {
        String token = bearerToken(mockMvc);
        String created = createJeriva(token);
        Integer id = JsonPath.read(created, "$.id");
        List<String> urls = JsonPath.read(created, "$.images[*].url");

        mockMvc.perform(delete("/plants/" + id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/plants/" + id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
        assertThat(urls).hasSize(2);
        for (String url : urls) {
            mockMvc.perform(get(URI.create(url))).andExpect(status().isNotFound());
        }
    }

    private String createJeriva(String token) throws Exception {
        return mockMvc.perform(multipart("/plants")
                        .file(json(JERIVA))
                        .file(png("jeriva-elevacao.png"))
                        .file(png("jeriva-vista-superior.png"))
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private static MockMultipartFile json(String content) {
        return new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, content.getBytes());
    }

    private static MockMultipartFile png(String filename) {
        return new MockMultipartFile("files", filename, MediaType.IMAGE_PNG_VALUE, TestPngs.transparent(3, 3));
    }
}
