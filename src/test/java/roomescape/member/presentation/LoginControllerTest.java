package roomescape.member.presentation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DatabaseCleaner;
import roomescape.member.presentation.dto.TokenRequest;
import roomescape.member.presentation.fixture.MemberFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LoginControllerTest {
    private final DatabaseCleaner databaseCleaner;
    private final MemberFixture memberFixture = new MemberFixture();

    @LocalServerPort
    int port;

    @Autowired
    LoginControllerTest(final DatabaseCleaner databaseCleaner) {
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clear();
        databaseCleaner.setUserInfo();
    }

    @Test
    @DisplayName("로그인 테스트")
    void loginTest() {
        // given
        final TokenRequest tokenRequest = memberFixture.createLoginRequest("user@user.com", "user");

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @ParameterizedTest
    @CsvSource(value = {"user@user.com:admin", ":admin", "admin:"}, delimiter = ':')
    @DisplayName("로그인 실패 테스트")
    void when_login_fail_then_throw_bad_request(String email, String password) {
        // given
        final TokenRequest tokenRequest = memberFixture.createLoginRequest(email, password);

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("로그인 체크 테스트")
    void loginCheckTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginUser();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("로그인 체크 실패 테스트")
    void loginCheckFailTest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }
}
