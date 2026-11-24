package com.example.enterprise;

import com.example.enterprise.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flyway_ShouldHaveAppliedAllMigrations() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
                Integer.class);

        assertThat(count).isNotNull().isGreaterThanOrEqualTo(2);
    }

    @Test
    void flyway_ShouldRecordV1AndV2Migration() {
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class);

        assertThat(versions).contains("1", "2");
    }

    @Test
    void flyway_ShouldCreateAllExpectedTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name",
                String.class);

        assertThat(tables).contains(
                "products",
                "users",
                "user_roles",
                "audit_log",
                "flyway_schema_history"
        );
    }

    @Test
    void flyway_ShouldCreateUniqueConstraintOnProductName() {
        List<String> constraints = jdbcTemplate.queryForList(
                "SELECT constraint_name FROM information_schema.table_constraints " +
                "WHERE table_name = 'products' AND constraint_type = 'UNIQUE'",
                String.class);

        assertThat(constraints).anyMatch(c -> c.toLowerCase().contains("name"));
    }

    @Test
    void flyway_ShouldCreateExpectedIndexes() {
        List<String> indexes = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE schemaname = 'public' ORDER BY indexname",
                String.class);

        assertThat(indexes)
                .contains("idx_products_name")
                .contains("idx_audit_log_product_id")
                .contains("idx_audit_log_timestamp")
                .contains("idx_products_name_lower")
                .contains("idx_audit_log_product_timestamp");
    }

    @Test
    void flyway_ShouldCreateForeignKeyOnUserRoles() {
        List<String> constraints = jdbcTemplate.queryForList(
                "SELECT constraint_name FROM information_schema.table_constraints " +
                "WHERE table_name = 'user_roles' AND constraint_type = 'FOREIGN KEY'",
                String.class);

        assertThat(constraints).isNotEmpty();
    }
}
