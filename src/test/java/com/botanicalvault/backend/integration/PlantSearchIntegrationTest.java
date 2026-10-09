package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.bearerToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.botanicalvault.backend.TestPngs;
import com.botanicalvault.backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlantSearchIntegrationTest {
    private static final String QUERY = "iris-busca";
    private static final String FIRST =
            "{\"commonName\":\"Íris-busca Um\",\"typeId\":1,\"sizeId\":1,\"originIds\":[1],"
                    + "\"lightExposures\":[\"PARTIAL_SHADE\",\"FULL_SUN\"],\"images\":[{\"viewId\":1}]}";
    private static final String SECOND =
            "{\"commonName\":\"Íris-busca Dois\",\"typeId\":2,\"sizeId\":3,\"images\":[{\"viewId\":2}]}";
    private static final String THIRD =
            "{\"commonName\":\"Íris-busca Três\",\"typeId\":1,\"sizeId\":2,\"originIds\":[1,2],"
                    + "\"lightExposures\":[\"FULL_SUN\"],\"images\":[{\"viewId\":1},{\"viewId\":3}]}";

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = bearerToken(mockMvc);
        String existing = mockMvc.perform(search().param("q", QUERY))
                .andReturn().getResponse().getContentAsString();
        if (existing.contains("\"plants\":[]")) {
            create(FIRST, 1);
            create(SECOND, 1);
            create(THIRD, 2);
        }
    }

    @Test
    void searchesWithoutAccentsSortedByName() throws Exception {
        mockMvc.perform(search().param("q", QUERY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants.length()").value(3))
                .andExpect(jsonPath("$.plants[0].commonName").value("Íris-busca Dois"))
                .andExpect(jsonPath("$.plants[1].commonName").value("Íris-busca Três"))
                .andExpect(jsonPath("$.plants[2].commonName").value("Íris-busca Um"))
                .andExpect(jsonPath("$.plants[1].imageCount").value(2))
                .andExpect(jsonPath("$.plants[1].origins.length()").value(2));
    }

    @Test
    void countsIgnoreTheirOwnGroup() throws Exception {
        mockMvc.perform(search().param("q", QUERY).param("typeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants.length()").value(2))
                .andExpect(jsonPath("$.facets.types.all").value(3))
                .andExpect(jsonPath("$.facets.sizes.all").value(2))
                .andExpect(jsonPath("$.facets.origins.unclassified").value(0))
                .andExpect(jsonPath("$.facets.views.options.length()").value(2));
    }

    @Test
    void detailKeepsEachImageOnceWhenPlantHasManyOrigins() throws Exception {
        String body = mockMvc.perform(search().param("q", "iris-busca tres"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.plants[0].id");

        mockMvc.perform(get("/plants/" + id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.origins.length()").value(2));
    }

    @Test
    void filtersUnclassifiedPlants() throws Exception {
        mockMvc.perform(search().param("q", QUERY).param("unclassified", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants.length()").value(1))
                .andExpect(jsonPath("$.plants[0].commonName").value("Íris-busca Dois"));
    }

    @Test
    void filtersByLightExposure() throws Exception {
        mockMvc.perform(search().param("q", QUERY).param("lightExposure", "FULL_SUN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants.length()").value(2))
                .andExpect(jsonPath("$.plants[0].lightExposures[0]").value("FULL_SUN"))
                .andExpect(jsonPath("$.facets.lightExposures.all").value(3));
        mockMvc.perform(search().param("q", QUERY).param("lightExposure", "SHADE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants.length()").value(0));
    }

    @Test
    void sortsNewestFirst() throws Exception {
        mockMvc.perform(search().param("q", QUERY).param("sort", "RECENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants[0].commonName").value("Íris-busca Três"));
    }

    @Test
    void rejectsOriginTogetherWithUnclassified() throws Exception {
        mockMvc.perform(search().param("originId", "1").param("unclassified", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CONFLICTING_FILTERS"));
    }

    @Test
    void searchRequiresToken() throws Exception {
        mockMvc.perform(get("/plants")).andExpect(status().isUnauthorized());
    }

    private MockHttpServletRequestBuilder search() {
        return get("/plants").header(HttpHeaders.AUTHORIZATION, token);
    }

    private void create(String plantJson, int imageCount) throws Exception {
        var request = multipart("/plants")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, plantJson.getBytes()));
        for (int index = 0; index < imageCount; index++) {
            request.file(new MockMultipartFile("files", "iris.png", MediaType.IMAGE_PNG_VALUE, TestPngs.transparent(2, 2)));
        }
        mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isCreated());
    }
}
