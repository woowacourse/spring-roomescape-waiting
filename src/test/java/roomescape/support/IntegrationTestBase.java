package roomescape.support;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    private static final int H2_TCP_PORT = 1521;

    static final GenericContainer<?> H2_CONTAINER =
        new GenericContainer<>(DockerImageName.parse("oscarfonts/h2"))
            .withExposedPorts(H2_TCP_PORT)
            .withEnv("H2_OPTIONS", "-ifNotExists");

    static {
        H2_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        String host = H2_CONTAINER.getHost();
        Integer port = H2_CONTAINER.getMappedPort(H2_TCP_PORT);

        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.url", () -> "jdbc:h2:tcp://" + host + ":" + port + "/test");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @LocalServerPort
    protected int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUpIntegrationTest() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }
}
