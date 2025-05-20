package roomescape.common;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CleanUp {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void all() {
        List<String> tableNames = jdbcTemplate.query("""
                select TABLE_NAME
                from INFORMATION_SCHEMA.TABLES
                where TABLE_SCHEMA = 'PUBLIC';
                """, (rs, rowNum) -> rs.getString("TABLE_NAME"));

        enableForeignKeyConstraints(false);
        tableNames.forEach(tableName -> {
            jdbcTemplate.update("TRUNCATE TABLE " + tableName + " RESTART IDENTITY;");
        });
        enableForeignKeyConstraints(true);
    }

    private void enableForeignKeyConstraints(boolean enable) {
        String value = enable ? "TRUE" : "FALSE";
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY " + value + ";");
    }
}
