package roomescape.member.presentation;

import static org.hamcrest.Matchers.greaterThan;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DatabaseCleaner;
import roomescape.member.presentation.dto.SignUpRequest;
import roomescape.member.presentation.fixture.MemberFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberControllerTest {
    private final DatabaseCleaner databaseCleaner;
    private final MemberFixture memberFixture = new MemberFixture();

    @LocalServerPort
    int port;

    @Autowired
    MemberControllerTest(final DatabaseCleaner databaseCleaner) {
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clear();
        databaseCleaner.setUserInfo();
    }

    @Test
    @DisplayName("회원가입 테스트")
    void loginTest() {
        // given
        final SignUpRequest signUpRequest = new SignUpRequest("test@test.com", "test", "테스트");

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/signUp")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("전체 회원 조회 테스트")
    void getMembersTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("비관리자 사용자는 전체 회원 조회가 불가능")
    void getMembersWithNonAdminTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginUser();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/members")
                .then().log().all()
                .statusCode(403);
    }
}
