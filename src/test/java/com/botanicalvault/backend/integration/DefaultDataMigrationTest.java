package com.botanicalvault.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.botanicalvault.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class DefaultDataMigrationTest {

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void seedsTaxonomiesInOrder() {
        assertThat(namesOf("origins")).containsExactly("Floresta Ombrófila Mista", "Mata Atlântica");
        assertThat(namesOf("plant_types")).containsExactly("Forração", "Arbusto", "Árvore", "Palmeira");
        assertThat(namesOf("sizes")).containsExactly("Pequeno", "Médio", "Grande");
        assertThat(namesOf("image_views")).containsExactly("Elevação", "Vista superior", "Perspectiva");
    }

    @Test
    void seedsSingleFavoritesCollection() {
        List<String> favorites = jdbcClient.sql("SELECT name FROM collections WHERE is_favorites")
                .query(String.class).list();

        assertThat(favorites).containsExactly("Favoritos");
    }

    private List<String> namesOf(String table) {
        return jdbcClient.sql("SELECT name FROM " + table + " ORDER BY position").query(String.class).list();
    }
}
