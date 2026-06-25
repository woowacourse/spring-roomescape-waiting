package roomescape.controller;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class AdminReservationSlotControllerTest {

    @DisplayName("모든 사용자의 예약 내역이 모두 조회되어야한다.")
    @Test
    void 관리자_예약_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(22))
                .body("[0].id", is(1))
                .body("[0].date", is("2026-04-29"))
                .body("[0].themeName", is("공포의 저택"))
                .body("[0].time", is("12:00"))
                .body("find { it.id == 22 }.date", is("2026-06-03"))
                .body("find { it.id == 22 }.themeName", is("탐정 사무소"))
                .body("find { it.id == 22 }.time", is("22:00"));

    }

}
