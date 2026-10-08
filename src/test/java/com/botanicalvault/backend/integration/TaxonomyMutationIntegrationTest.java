package com.botanicalvault.backend.integration;

import static com.botanicalvault.backend.integration.AuthTestSupport.bearerToken;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaxonomyMutationIntegrationTest {
    private static final int SHRUB_ID = 2;
    private static final int MIXED_FOREST_ID = 1;
    private static final int ELEVATION_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = bearerToken(mockMvc);
    }

    @Test
    void reassigningTypeMovesItsPlants() throws Exception {
        int typeId = createItem("/plant-types", "Trepadeira IT");
        int plantId = createPlant("Hera IT", "\"typeId\":" + typeId + ",\"sizeId\":1", ELEVATION_ID);
        perform(get("/plant-types"))
                .andExpect(jsonPath("$[?(@.id == " + typeId + ")].usageCount").value(contains(1)));

        perform(delete("/plant-types/" + typeId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("REASSIGNMENT_REQUIRED"));
        perform(delete("/plant-types/" + typeId).param("reassignTo", String.valueOf(SHRUB_ID)))
                .andExpect(status().isNoContent());

        perform(get("/plants/" + plantId)).andExpect(jsonPath("$.type.id").value(SHRUB_ID));
        perform(get("/plant-types")).andExpect(jsonPath("$[*].id").value(not(hasItem(typeId))));
    }

    @Test
    void reassigningOriginKeepsOneLinkPerPlant() throws Exception {
        int originId = createItem("/origins", "Cerrado IT");
        int plantId = createPlant("Ipê IT",
                "\"typeId\":1,\"sizeId\":1,\"originIds\":[" + originId + "," + MIXED_FOREST_ID + "]", ELEVATION_ID);

        perform(delete("/origins/" + originId).param("reassignTo", String.valueOf(MIXED_FOREST_ID)))
                .andExpect(status().isNoContent());

        perform(get("/plants/" + plantId)).andExpect(jsonPath("$.origins[*].id").value(contains(MIXED_FOREST_ID)));
    }

    @Test
    void deletingOriginWithoutTargetLeavesPlantUnclassified() throws Exception {
        int originId = createItem("/origins", "Pampa IT");
        createPlant("Butiá IT", "\"typeId\":1,\"sizeId\":1,\"originIds\":[" + originId + "]", ELEVATION_ID);

        perform(delete("/origins/" + originId)).andExpect(status().isNoContent());

        perform(get("/plants").param("q", "Butiá IT").param("unclassified", "true"))
                .andExpect(jsonPath("$.plants[*].commonName").value(contains("Butiá IT")));
    }

    @Test
    void reassigningViewMovesItsImages() throws Exception {
        int viewId = createItem("/image-views", "Detalhe IT");
        int plantId = createPlant("Bromélia IT", "\"typeId\":1,\"sizeId\":1", viewId);

        perform(delete("/image-views/" + viewId).param("reassignTo", String.valueOf(ELEVATION_ID)))
                .andExpect(status().isNoContent());

        perform(get("/plants/" + plantId)).andExpect(jsonPath("$.images[0].view.id").value(ELEVATION_ID));
    }

    @Test
    void reorderIsReflectedInTheList() throws Exception {
        String body = perform(get("/sizes")).andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(body, "$[*].id");
        List<Integer> reversed = ids.reversed();

        perform(put("/sizes/order").contentType(MediaType.APPLICATION_JSON).content(order(reversed)))
                .andExpect(status().isNoContent());
        perform(get("/sizes")).andExpect(jsonPath("$[*].id").value(contains(reversed.toArray())));

        perform(put("/sizes/order").contentType(MediaType.APPLICATION_JSON).content(order(ids)))
                .andExpect(status().isNoContent());
    }

    @Test
    void renameRejectsExistingName() throws Exception {
        perform(patch("/plant-types/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"arbusto\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_NAME"));
    }

    private int createItem(String route, String name) throws Exception {
        String body = perform(post(route).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private int createPlant(String name, String fields, int viewId) throws Exception {
        String json = "{\"commonName\":\"" + name + "\"," + fields + ",\"images\":[{\"viewId\":" + viewId + "}]}";
        var request = multipart("/plants")
                .file(new MockMultipartFile("plant", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes()))
                .file(new MockMultipartFile("files", "plant.png", MediaType.IMAGE_PNG_VALUE, TestPngs.transparent(2, 2)));
        String body = mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static String order(List<Integer> ids) {
        return "{\"ids\":" + ids + "}";
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, token));
    }
}
