package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.botanicalvault.backend.TestUsage;
import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.constants.Taxonomy;
import com.botanicalvault.backend.entity.Origin;
import com.botanicalvault.backend.entity.PlantType;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.repository.OriginRepository;
import com.botanicalvault.backend.repository.PlantTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class TaxonomyCatalogTest {
    private static final long SHRUB_ID = 2L;
    private static final long TREE_ID = 3L;
    private final PlantTypeRepository typeRepository = mock(PlantTypeRepository.class);
    private final OriginRepository originRepository = mock(OriginRepository.class);
    private final TaxonomyCatalog catalog = new TaxonomyCatalog();
    private final PlantType shrub = PlantType.builder().id(SHRUB_ID).name("Arbusto").position(2).build();
    private final PlantType tree = PlantType.builder().id(TREE_ID).name("Árvore").position(3).build();

    @Test
    void renameRejectsNameOfAnotherItem() {
        when(typeRepository.findById(SHRUB_ID)).thenReturn(Optional.of(shrub));
        when(typeRepository.existsByNameIgnoreCaseAndIdNot("árvore", SHRUB_ID)).thenReturn(true);

        assertError(() -> catalog.rename(typeRepository, Taxonomy.PLANT_TYPE, SHRUB_ID, " árvore "),
                ErrorCode.DUPLICATE_NAME);
    }

    @Test
    void renameTrimsName() {
        when(typeRepository.findById(SHRUB_ID)).thenReturn(Optional.of(shrub));

        assertThat(catalog.rename(typeRepository, Taxonomy.PLANT_TYPE, SHRUB_ID, " Arbustos ").getName())
                .isEqualTo("Arbustos");
    }

    @Test
    void reorderRewritesPositions() {
        when(typeRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(shrub, tree));

        catalog.reorder(typeRepository, List.of(TREE_ID, SHRUB_ID));

        assertThat(tree.getPosition()).isEqualTo(1);
        assertThat(shrub.getPosition()).isEqualTo(2);
    }

    @Test
    void deleteReassignsBeforeDeleting() {
        givenTypes(5);
        when(typeRepository.existsById(TREE_ID)).thenReturn(true);

        catalog.delete(typeRepository, Taxonomy.PLANT_TYPE, SHRUB_ID, TREE_ID);

        InOrder order = inOrder(typeRepository);
        order.verify(typeRepository).reassignUsages(SHRUB_ID, TREE_ID);
        order.verify(typeRepository).deleteById(SHRUB_ID);
    }

    @Test
    void deleteRejectsReassignmentToItself() {
        givenTypes(5);

        assertError(() -> catalog.delete(typeRepository, Taxonomy.PLANT_TYPE, SHRUB_ID, SHRUB_ID),
                ErrorCode.INVALID_REASSIGNMENT);
        verify(typeRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRejectsMissingTarget() {
        givenTypes(5);

        assertError(() -> catalog.delete(typeRepository, Taxonomy.PLANT_TYPE, SHRUB_ID, TREE_ID),
                ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void deleteRequiresTargetForUsedType() {
        givenTypes(5);

        assertError(() -> catalog.delete(typeRepository, Taxonomy.PLANT_TYPE, SHRUB_ID, null),
                ErrorCode.REASSIGNMENT_REQUIRED);
    }

    @Test
    void deleteUnassignsUsedOrigin() {
        when(originRepository.findById(1L)).thenReturn(Optional.of(Origin.builder().id(1L).name("Mata").build()));
        when(originRepository.countUsage()).thenReturn(List.of(new TestUsage(1L, 3)));
        when(originRepository.count()).thenReturn(2L);

        catalog.delete(originRepository, Taxonomy.ORIGIN, 1L, null);

        verify(originRepository, never()).reassignUsages(any(), any());
        verify(originRepository).deleteById(1L);
    }

    private void givenTypes(long usage) {
        when(typeRepository.findById(SHRUB_ID)).thenReturn(Optional.of(shrub));
        when(typeRepository.countUsage()).thenReturn(List.of(new TestUsage(SHRUB_ID, usage)));
        when(typeRepository.count()).thenReturn(4L);
    }

    private static void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }
}
