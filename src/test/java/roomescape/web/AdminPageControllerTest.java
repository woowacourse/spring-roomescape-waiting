package roomescape.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.common.config.IntegrationTest;
import roomescape.common.util.CookieUtils;

class AdminPageControllerTest extends IntegrationTest {

    @DisplayName("어드민 사용자 접근 성공 테스트")
    @Nested
    class AdminRoleTest {

        @DisplayName("/admin을 요청하면 html을 반환한다.")
        @Test
        void requestAdmin() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                    .when()
                    .get("/admin")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML);
        }

        @DisplayName("/admin/reservation을 요청하면 html을 반환한다.")
        @Test
        void requestAdminReservation() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                    .when()
                    .get("/admin/reservation")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML);
        }

        @DisplayName("/admin/reservation/waiting을 요청하면 html을 반환한다.")
        @Test
        void requestAdminWaitingReservation() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                    .when()
                    .get("/admin/reservation/waiting")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML);
        }

        @DisplayName("/admin/time을 요청하면 html을 반환한다.")
        @Test
        void requestTime() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                    .when()
                    .get("/admin/time")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML);
        }

        @DisplayName("/theme 을 요청하면 html을 반환한다.")
        @Test
        void requestTheme() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                    .when()
                    .get("/admin/theme")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML);
        }
    }

    @DisplayName("일반 사용자 접근 성공 테스트")
    @Nested
    class MemberRoleTest {

        @DisplayName("일반 사용자가 /admin을 요청하면 403 응답 코드를 반환한다.")
        @Test
        void AdminPathAccessDenied() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                    .when()
                    .get("/admin")
                    .then().log().all()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @DisplayName("일반 사용자가 /admin/reservation을 요청하면 403 응답 코드를 반환한다.")
        @Test
        void AdminReservationPathAccessDenied() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                    .when()
                    .get("/admin/reservation")
                    .then().log().all()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @DisplayName("일반 사용자가 /admin/time을 요청하면 403 응답 코드를 반환한다.")
        @Test
        void AdminTimePathAccessDenied() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                    .when()
                    .get("/admin/time")
                    .then().log().all()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @DisplayName("일반 사용자가 /admin/them를 요청하면 403 응답 코드를 반환한다.")
        @Test
        void AdminThemePathAccessDenied() {
            RestAssured.given().log().all()
                    .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                    .when()
                    .get("/admin/theme")
                    .then().log().all()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }
    }
}
