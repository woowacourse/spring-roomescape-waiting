package roomescape.acceptance_test;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestClockConfig.class)
@Sql(value = "/acceptance-cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
public abstract class AcceptanceTestSupport {

    @LocalServerPort
    protected int port;

    @Autowired
    protected MutableClock mutableClock;

    @BeforeEach
    void setUpAcceptanceTest() {
        mutableClock.reset();
        RestAssured.port = port;
    }

    @AfterEach
    void tearDownAcceptanceTest() {
        RestAssured.reset();
    }
}
