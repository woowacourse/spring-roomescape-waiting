package roomescape.util;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.auth.service.TokenProvider;

import static roomescape.fixture.MemberFixture.getMemberAdmin;
import static roomescape.fixture.MemberFixture.getMemberChoco;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ControllerTest {
    @LocalServerPort
    int port;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    protected TokenProvider tokenProvider;
    protected String adminToken;
    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @BeforeEach
    void setInitialData() {
        databaseCleaner.insertInitialData();
        adminToken = tokenProvider.createAccessToken(getMemberAdmin().getEmail());
    }

    @AfterEach
    void clearDatabase() {
        databaseCleaner.clear();
    }
}
