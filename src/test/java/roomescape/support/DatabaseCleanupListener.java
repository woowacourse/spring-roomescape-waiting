package roomescape.support;

import io.restassured.RestAssured;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DatabaseCleanupListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        String port = testContext.getApplicationContext()
                .getEnvironment()
                .getProperty("local.server.port");
        if (port != null) {
            RestAssured.port = Integer.parseInt(port);
        }

        JdbcTemplate jdbcTemplate = testContext.getApplicationContext()
                .getBean(JdbcTemplate.class);

        jdbcTemplate.update("DELETE FROM payment_order");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }
}
