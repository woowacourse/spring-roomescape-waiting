package roomescape.controller.admin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.BaseControllerTest;

class AdminPageControllerTest extends BaseControllerTest {

    @DisplayName("어드민 홈페이지에 접근한다.")
    @Test
    void responseAdminPage() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().get("/admin")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("어드민 예약 페이지에 접근한다.")
    @Test
    void responseAdminReservationPage() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().get("/admin/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("어드민 시간 페이지에 접근한다.")
    @Test
    void responseAdminTimePage() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().get("/admin/time")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("어드민 테마 페이지에 접근한다.")
    @Test
    void responseAdminThemePage() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().get("/admin/theme")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("어드민 예약 대기 페이지에 접근한다.")
    @Test
    void responseAdminWaitingPage() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().get("/admin/waiting")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("관리자가 아닌 회원은 접속할 수 없다.")
    @Test
    void responseAdminPageWithoutAdmin() {
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
