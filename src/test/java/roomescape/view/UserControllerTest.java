package roomescape.view;

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
import roomescape.member.presentation.fixture.MemberFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private final DatabaseCleaner databaseCleaner;
    private final MemberFixture memberFixture = new MemberFixture();

    @LocalServerPort
    int port;

    @Autowired
    UserControllerTest(final DatabaseCleaner databaseCleaner) {
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clear();
        databaseCleaner.setUserInfo();
    }

    @Test
    @DisplayName("예약 페이지 테스트")
    void reservationTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginUser();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 예약 페이지 접근 테스트")
    void unauthorizedReservationTest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(401); // 또는 리다이렉션 코드 302
    }

    @Test
    @DisplayName("로그인 페이지 테스트")
    void loginTest() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("회원가입 페이지 테스트")
    void signUpTest() {
        RestAssured.given().log().all()
                .when().get("/signup")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("메인 페이지 테스트")
    void indexTest() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }
}
