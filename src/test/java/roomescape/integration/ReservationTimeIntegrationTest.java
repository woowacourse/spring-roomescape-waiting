package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.ReservationTimeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/init-data.sql", "/controller-test-data.sql"})
class ReservationTimeIntegrationTest {
    @DisplayName("모든 예약 시간을 조회한다")
    @Test
    void should_get_reservation_times() {

        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .body("size()", equalTo(6))
                .body("startAt", hasItems("10:00", "11:00", "12:00", "13:00", "14:00", "15:00"));
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
                .assertThat()
                .statusCode(201)
                .and()
                .header("Location", response -> equalTo("/times/" + response.path("id")));
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_remove_reservation_time() {
        RestAssured.given().log().all()
                .pathParam("id", 5)
                .when().delete("/times/{id}")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("특정 날짜와 테마에 따른 모든 시간의 예약 가능 여부를 확인한다.")
    @Test
    void should_get_reservations_with_book_state_by_date_and_theme() {

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", "2030-08-05")
                .queryParam("themeId", 1)
                .when().get("/times/reserved")
                .then().log().all()
                .statusCode(200);
    }
}
