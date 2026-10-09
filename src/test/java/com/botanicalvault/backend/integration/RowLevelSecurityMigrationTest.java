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
class RowLevelSecurityMigrationTest {

    private static final String FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void enablesRowLevelSecurityOnEveryPublicTable() {
        List<String> tablesWithoutRls = jdbcClient
                .sql("SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND NOT rowsecurity AND tablename <> :history")
                .param("history", FLYWAY_HISTORY_TABLE)
                .query(String.class).list();

        assertThat(tablesWithoutRls).isEmpty();
    }

    @Test
    void keepsTablesReadableForTheApplicationRole() {
        Integer collections = jdbcClient.sql("SELECT count(*) FROM collections").query(Integer.class).single();

        assertThat(collections).isPositive();
    }
}
