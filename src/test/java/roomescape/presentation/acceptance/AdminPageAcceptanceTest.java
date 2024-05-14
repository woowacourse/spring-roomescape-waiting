package roomescape.presentation.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminPageAcceptanceTest extends AcceptanceTest {

    @DisplayName("요청자가 운영자라면 200 OK 응답을 받는다.")
    @Nested
    class Admin {
        private static String accessToken;

        @BeforeEach
        void setUp() {
            accessToken = RestAssured.given()
                    .contentType("application/json")
                    .body("{\"email\":\"admin@wooteco.com\", \"password\":\"wootecoCrew6!\"}")
                    .when().post("/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .cookie("token");
        }

        @DisplayName("admin 요청하면 200 OK 응답한다.")
        @Test
        void adminPageTest() {
            RestAssured.given().log().all()
                    .cookie("token", accessToken)
                    .when().get("/admin")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("예약 페이지를 요청하면 200 OK 응답한다.")
        @Test
        void reservationPageTest() {
            RestAssured.given().log().all()
                    .cookie("token", accessToken)
                    .when().get("/admin/reservation")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("시간 추가 페이지를 요청하면 200 OK 응답한다.")
        @Test
        void timePageTest() {
            RestAssured.given().log().all()
                    .cookie("token", accessToken)
                    .when().get("/admin/time")
                    .then().log().all()
                    .statusCode(200);
        }
    }

    @DisplayName("요청자가 운영자가 아니라면 401 응답을 받는다.")
    @Test
    void basicMemberUnAuthorized() {
        RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }
}
