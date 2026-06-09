package roomescape.support;

import io.restassured.RestAssured;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class SetupListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(TestContext testContext) {
        String port = testContext.getApplicationContext()
                .getEnvironment()
                .getProperty("local.server.port");
        RestAssured.port = Integer.parseInt(port);
    }
}
