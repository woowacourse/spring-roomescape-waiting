package roomescape.controller.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.IntegrationTestSupport;
import roomescape.controller.reservation.dto.CreateReservationRequest;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationControllerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("ADMIN이 예약 관리 페이지로 이동")
    void moveToAdminReservationPage() {
        RestAssured.given().log().all()
                .cookie("token", ADMIN_TOKEN)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("ADMIN이 시간 관리 페이지로 이동")
    void moveToAdminTimePage() {
        RestAssured.given().log().all()
                .cookie("token", ADMIN_TOKEN)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("ADMIN이 테마 관리 페이지로 이동")
    void moveToAdminThemePage() {
        RestAssured.given().log().all()
                .cookie("token", ADMIN_TOKEN)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("ADMIN이 예약 생성")
    void createReservationAdmin() {
        final CreateReservationRequest request = new CreateReservationRequest(1L,
                1L, LocalDate.now().plusDays(1), 1L);

        RestAssured.given().log().all()
                .cookie("token", ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("ADMIN이 예약 대기 목록 조회")
    void getWaitReservation() {
        RestAssured.given().log().all()
                .cookie("token", ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
