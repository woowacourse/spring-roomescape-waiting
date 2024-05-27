package roomescape.util;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DatabaseCleaner extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        JdbcTemplate jdbcTemplate = testContext.getApplicationContext().getBean(JdbcTemplate.class);
        List<String> queries = getTruncateQueries(jdbcTemplate);
        truncate(queries, jdbcTemplate);
    }

    private List<String> getTruncateQueries(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForList( """
            SELECT CONCAT('TRUNCATE TABLE ', TABLE_NAME, ' RESTART IDENTITY;') 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = 'PUBLIC';
            """, String.class);
    }

    private void truncate(List<String> queries, JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        queries.forEach(jdbcTemplate::execute);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
    }
}
