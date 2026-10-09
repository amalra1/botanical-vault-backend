package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.bearerToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlantIntegrationTest {
    private static final String BUTIA =
            "{\"commonName\":\"Butiá\",\"scientificName\":\"Butia eriospatha\",\"typeId\":4,\"sizeId\":2,"
                    + "\"originIds\":[1],\"lightExposures\":[\"FULL_SUN\",\"PARTIAL_SHADE\"],"
                    + "\"images\":[{\"viewId\":1},{\"viewId\":2}]}";
    private static final byte[] COVER = TestPngs.transparent(4, 6);
    private static final byte[] TOP_VIEW = TestPngs.transparent(5, 5);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsPlantAndServesItsImages() throws Exception {
        String token = bearerToken(mockMvc);
        MockMultipartHttpServletRequestBuilder request = plantRequest(BUTIA)
                .file(png("butia-elevacao.png", COVER))
                .file(png("butia-vista-superior.png", TOP_VIEW));

        String body = mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type.name").value("Palmeira"))
                .andExpect(jsonPath("$.origins[0].name").value("Floresta Ombrófila Mista"))
                .andExpect(jsonPath("$.images[0].originalFilename").value("butia-elevacao.png"))
                .andExpect(jsonPath("$.images[1].view.name").value("Vista superior"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");

        String detail = mockMvc.perform(get("/plants/" + id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.lightExposures.length()").value(2))
                .andExpect(jsonPath("$.lightExposures[0]").value("PARTIAL_SHADE"))
                .andExpect(jsonPath("$.lightExposures[1]").value("FULL_SUN"))
                .andReturn().getResponse().getContentAsString();
        String coverUrl = JsonPath.read(detail, "$.images[0].url");

        mockMvc.perform(get(URI.create(coverUrl)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(COVER));
    }

    @Test
    void rejectsRenamedJpeg() throws Exception {
        String plant = "{\"commonName\":\"Foto\",\"typeId\":1,\"sizeId\":1,\"images\":[{\"viewId\":1}]}";
        MockMultipartHttpServletRequestBuilder request = plantRequest(plant)
                .file(png("foto.png", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}));

        mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearerToken(mockMvc)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("INVALID_IMAGE_FORMAT"));
    }

    @Test
    void createRequiresToken() throws Exception {
        mockMvc.perform(plantRequest(BUTIA).file(png("a.png", COVER)))
                .andExpect(status().isUnauthorized());
    }

    private static MockMultipartHttpServletRequestBuilder plantRequest(String plantJson) {
        return multipart("/plants")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, plantJson.getBytes()));
    }

    private static MockMultipartFile png(String filename, byte[] content) {
        return new MockMultipartFile("files", filename, MediaType.IMAGE_PNG_VALUE, content);
    }
}
