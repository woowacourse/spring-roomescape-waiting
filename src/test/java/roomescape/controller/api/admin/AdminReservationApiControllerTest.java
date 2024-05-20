package roomescape.controller.api.admin;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.BaseControllerTest;
import roomescape.util.TokenGenerator;

class AdminReservationApiControllerTest extends BaseControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("관리자 예약 페이지 요청이 정상적으로 수행된다.")
    void moveToReservationPage_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("관리자 예약 페이지에 권한이 없는 유저는 401을 받는다.")
    void moveToReservationPage_Failure() {
        RestAssured.given().log().all()
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("전체 예약 목록 조회 요청이 정상적으로 수행된다.")
    void selectReservationListRequest_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1));
    }

    @Test
    @DisplayName("관리자가 예약 추가, 조회를 정상적으로 수행한다.")
    void Reservation_CREATE_READ_Success() {
        Map<String, Object> reservation = Map.of(
                "memberId", 1,
                "date", LocalDate.now().plusDays(2L).toString(),
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeAdminToken())
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2));
    }
}
