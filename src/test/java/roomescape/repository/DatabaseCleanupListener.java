package roomescape.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DatabaseCleanupListener extends AbstractTestExecutionListener {

    private final String sql = """
            SELECT CONCAT('TRUNCATE TABLE ', TABLE_NAME, ' RESTART IDENTITY;') 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = 'PUBLIC';
            """;

    @Override
    public void beforeTestMethod(TestContext testContext) {
        JdbcTemplate jdbcTemplate = getJdbcTemplateBean(testContext);
        List<String> queries = jdbcTemplate.queryForList(sql, String.class);
        execute(queries, jdbcTemplate);
    }

    private JdbcTemplate getJdbcTemplateBean(TestContext testContext) {
        return testContext.getApplicationContext().getBean(JdbcTemplate.class);
    }

    private static void execute(List<String> queries, JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        queries.forEach(jdbcTemplate::execute);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
    }
}
