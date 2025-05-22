package roomescape.view;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.presentation.fixture.MemberFixture;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminControllerTest {
    private final MemberFixture memberFixture = new MemberFixture();

    @Test
    @DisplayName("어드민 페이지 테스트")
    void adminTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 어드민 페이지 테스트")
    void unauthenticatedUserCannotAccessAdminPage() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("일반 사용자의 어드민 페이지 테스트")
    void regularUserCannotAccessAdminPage() {
        // given
        final Map<String, String> cookies = memberFixture.loginUser();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("예약 페이지 테스트")
    void reservationPageTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 시간 페이지 테스트")
    void reservationTimePageTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("테마 페이지 테스트")
    void themePageTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 대기 관리 페이지 테스트")
    void waitingPageTest() {
        // given
        final Map<String, String> cookies = memberFixture.loginAdmin();

        // when - then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().get("/admin/waiting")
                .then().log().all()
                .statusCode(200);
    }
}
