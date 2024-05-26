package roomescape.presentation.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminPageAcceptanceTest extends AcceptanceTest {

    @DisplayName("요청자가 운영자라면 200 OK 응답을 받는다.")
    @Nested
    class Admin {

        @DisplayName("admin 요청하면 200 OK 응답한다.")
        @Test
        void adminPageTest() {
            adminTokenSetup();

            RestAssured.given().log().all()
                    .cookie("token", adminToken)
                    .when().get("/admin")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("예약 페이지를 요청하면 200 OK 응답한다.")
        @Test
        void reservationPageTest() {
            adminTokenSetup();

            RestAssured.given().log().all()
                    .cookie("token", adminToken)
                    .when().get("/admin/reservation")
                    .then().log().all()
                    .statusCode(200);
        }

        @DisplayName("시간 추가 페이지를 요청하면 200 OK 응답한다.")
        @Test
        void timePageTest() {
            adminTokenSetup();

            RestAssured.given().log().all()
                    .cookie("token", adminToken)
                    .when().get("/admin/time")
                    .then().log().all()
                    .statusCode(200);
        }
    }

    @DisplayName("요청자가 운영자가 아니라면 401 응답을 받는다.")
    @Test
    void basicMemberUnAuthorized() {
        memberTokenSetUp();

        RestAssured.given().log().all()
                .cookie("token", memberToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }
}
