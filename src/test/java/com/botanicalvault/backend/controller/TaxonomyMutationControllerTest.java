package com.botanicalvault.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.botanicalvault.backend.config.SecurityConfig;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.TaxonomyOrderRequestDTO;
import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.User;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.repository.UserRepository;
import com.botanicalvault.backend.service.AuthUserDetailsService;
import com.botanicalvault.backend.service.ImageViewService;
import com.botanicalvault.backend.service.JwtService;
import com.botanicalvault.backend.service.OriginService;
import com.botanicalvault.backend.service.PlantTypeService;
import com.botanicalvault.backend.service.SizeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest({OriginController.class, PlantTypeController.class, SizeController.class, ImageViewController.class})
@Import({SecurityConfig.class, AuthUserDetailsService.class})
class TaxonomyMutationControllerTest {
    private static final String EMAIL = "architect@test.local";
    private static final String TOKEN = "valid-token";
    private static final String NAME_BODY = "{\"name\":\"Trepadeira\"}";
    private static final String ORDER_BODY = "{\"ids\":[2,1]}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OriginService originService;

    @MockitoBean
    private PlantTypeService plantTypeService;

    @MockitoBean
    private SizeService sizeService;

    @MockitoBean
    private ImageViewService imageViewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        when(jwtService.readSubject(TOKEN)).thenReturn(Optional.of(EMAIL));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(User.builder().email(EMAIL).build()));
    }

    static Stream<MockHttpServletRequestBuilder> mutations() {
        return Stream.of("/origins", "/plant-types", "/sizes", "/image-views").flatMap(route -> Stream.of(
                json(post(route), NAME_BODY),
                json(patch(route + "/1"), NAME_BODY),
                json(put(route + "/order"), ORDER_BODY),
                delete(route + "/1")));
    }

    @ParameterizedTest
    @MethodSource("mutations")
    void requiresToken(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("mutations")
    void rejectsInvalidToken(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsPlantType() throws Exception {
        when(plantTypeService.create(any())).thenReturn(new TaxonomyItemResponseDTO(5L, "Trepadeira"));

        mockMvc.perform(authorized(json(post("/plant-types"), NAME_BODY)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void updatesSizeWithDescription() throws Exception {
        when(sizeService.update(eq(1L), any())).thenReturn(new SizeResponseDTO(1L, "GG", "muito alta"));

        mockMvc.perform(authorized(json(patch("/sizes/1"), "{\"name\":\"GG\",\"description\":\"muito alta\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("muito alta"));
    }

    @Test
    void updateRejectsBlankName() throws Exception {
        mockMvc.perform(authorized(json(patch("/image-views/1"), "{\"name\":\" \"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void reordersOrigins() throws Exception {
        mockMvc.perform(authorized(json(put("/origins/order"), ORDER_BODY)))
                .andExpect(status().isNoContent());

        verify(originService).reorder(new TaxonomyOrderRequestDTO(List.of(2L, 1L)));
    }

    @Test
    void reorderRejectsEmptyIds() throws Exception {
        mockMvc.perform(authorized(json(put("/sizes/order"), "{\"ids\":[]}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.ids").exists());
    }

    @Test
    void deletesWithReassignment() throws Exception {
        mockMvc.perform(authorized(delete("/plant-types/2").param("reassignTo", "3")))
                .andExpect(status().isNoContent());

        verify(plantTypeService).delete(2L, 3L);
    }

    @Test
    void deleteReportsMissingReassignment() throws Exception {
        doThrow(new BusinessException(ErrorCode.REASSIGNMENT_REQUIRED, "Size is in use"))
                .when(sizeService).delete(1L, null);

        mockMvc.perform(authorized(delete("/sizes/1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(ErrorCode.REASSIGNMENT_REQUIRED.name()));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN);
    }
}
