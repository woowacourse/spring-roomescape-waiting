package roomescape.common;

import org.springframework.jdbc.core.JdbcTemplate;

public class DBInitializer {

    public static void truncate(final JdbcTemplate jdbcTemplate) {
        var sql = """
                select TABLE_NAME
                from INFORMATION_SCHEMA.TABLES
                where TABLE_SCHEMA = 'PUBLIC';
                """;
        var tableNames = jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getString("TABLE_NAME"));
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE;");
        tableNames.forEach(tableName ->
                jdbcTemplate.update(String.format("TRUNCATE TABLE %s RESTART IDENTITY;", tableName)));
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE;");
    }
}
