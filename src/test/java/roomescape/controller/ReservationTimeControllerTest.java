package roomescape.controller;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.IsReservedTimeResponse;
import roomescape.model.ReservationTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeControllerTest {
    @DisplayName("모든 예약 시간을 조회한다")
    @Test
    void should_get_reservation_times() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationTime.class);
    }

    @DisplayName("예약 시간을 추가한다")
    @Test
    void should_add_reservation_times() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(16, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/times/7");
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_remove_reservation_time() {
        RestAssured.given().log().all()
                .when().delete("/times/5")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("특정 날짜와 테마에 따른 모든 시간의 예약 가능 여부를 확인한다.")
    @Test
    void should_get_reservations_with_book_state_by_date_and_theme() {
        RestAssured.given().log().all()
                .when().get("/times/reserved?date=2030-08-05&themeId=1")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", IsReservedTimeResponse.class);
    }
}
