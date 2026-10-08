package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.bearerToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CollectionIntegrationTest {
    private static final String TALL =
            "{\"commonName\":\"Jerivá-coleção\",\"typeId\":4,\"sizeId\":3,\"images\":[{\"viewId\":1}]}";
    private static final String SHORT =
            "{\"commonName\":\"Grama-coleção\",\"typeId\":1,\"sizeId\":1,\"images\":[{\"viewId\":1}]}";

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = bearerToken(mockMvc);
    }

    @Test
    void buildsStaircaseCoverageAndMosaic() throws Exception {
        Integer collectionId = createCollection("Residência Batel — jardim");
        Integer tall = createPlant(TALL);
        Integer shortPlant = createPlant(SHORT);
        perform(put(memberPath(collectionId, tall))).andExpect(status().isNoContent());
        perform(put(memberPath(collectionId, shortPlant))).andExpect(status().isNoContent());
        perform(put(memberPath(collectionId, shortPlant))).andExpect(status().isNoContent());

        perform(get("/collections/" + collectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants.length()").value(2))
                .andExpect(jsonPath("$.plants[0].commonName").value("Grama-coleção"))
                .andExpect(jsonPath("$.sizeCoverage[0].covered").value(true))
                .andExpect(jsonPath("$.sizeCoverage[1].covered").value(false))
                .andExpect(jsonPath("$.sizeCoverage[2].covered").value(true));
        perform(get("/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].favorites").value(true))
                .andExpect(jsonPath("$[?(@.id == " + collectionId + ")].plantCount").value(2))
                .andExpect(jsonPath("$[?(@.id == " + collectionId + ")].mosaicUrls.length()").value(2));

        perform(delete("/plants/" + tall)).andExpect(status().isNoContent());
        perform(get("/collections/" + collectionId)).andExpect(jsonPath("$.plants.length()").value(1));
    }

    @Test
    void favoriteShowsInPlantResponses() throws Exception {
        Integer plantId = createPlant(TALL);
        Integer collectionId = createCollection("Favoritos de integração");
        perform(put(memberPath(collectionId, plantId))).andExpect(status().isNoContent());
        perform(put("/plants/" + plantId + "/favorite")).andExpect(status().isNoContent());

        perform(get("/plants/" + plantId))
                .andExpect(jsonPath("$.favorite").value(true))
                .andExpect(jsonPath("$.collectionIds.length()").value(2));
        perform(get("/plants").param("q", "jeriva-colecao"))
                .andExpect(jsonPath("$.plants[?(@.id == " + plantId + ")].favorite").value(true));

        perform(delete("/plants/" + plantId + "/favorite")).andExpect(status().isNoContent());
        perform(get("/plants/" + plantId)).andExpect(jsonPath("$.favorite").value(false));
    }

    @Test
    void renamesDeletesAndLocksFavorites() throws Exception {
        Integer collectionId = createCollection("Coleção temporária");
        perform(post("/collections").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"coleção TEMPORÁRIA\"}"))
                .andExpect(status().isConflict());
        perform(patch("/collections/" + collectionId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coleção renomeada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coleção renomeada"));
        perform(delete("/collections/" + collectionId)).andExpect(status().isNoContent());
        perform(get("/collections/" + collectionId)).andExpect(status().isNotFound());

        String collections = perform(get("/collections")).andReturn().getResponse().getContentAsString();
        Integer favoritesId = JsonPath.read(collections, "$[0].id");
        perform(delete("/collections/" + favoritesId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("FAVORITES_LOCKED"));
    }

    private Integer createCollection(String name) throws Exception {
        String body = perform(post("/collections").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private Integer createPlant(String plantJson) throws Exception {
        MockMultipartHttpServletRequestBuilder request = multipart("/plants")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, plantJson.getBytes()))
                .file(new MockMultipartFile("files", "planta.png", MediaType.IMAGE_PNG_VALUE,
                        TestPngs.transparent(2, 2)));
        String body = perform(request).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private ResultActions perform(AbstractMockHttpServletRequestBuilder<?> request) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, token));
    }

    private static String memberPath(Integer collectionId, Integer plantId) {
        return "/collections/" + collectionId + "/plants/" + plantId;
    }
}
