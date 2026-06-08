package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationApiIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiIntegrationTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new ApiIntegrationTestHelper(jdbcTemplate);
        testHelper.clearDatabase();
    }

    @DisplayName("방탈출 예약 추가 API를 테스트합니다.")
    @Test
    void save_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-06");
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("이름으로 본인 예약 목록 조회 API를 테스트합니다.")
    @Test
    void find_my_reservations() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);
        testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 7), themeId, secondTimeId);
        testHelper.insertReservation("카야", LocalDate.of(2028, 5, 8), themeId, secondTimeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", equalTo("스타크"))
                .body("[1].name", equalTo("스타크"));
    }

    @DisplayName("본인 예약 변경 API를 테스트합니다.")
    @Test
    void update_my_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long firstTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long secondTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, firstTimeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "스타크");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(secondTimeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(reservationId.intValue()))
                .body("date", equalTo("2028-05-07"))
                .body("time.id", equalTo(secondTimeId.intValue()));
    }

    @DisplayName("본인 예약 취소 API를 테스트합니다.")
    @Test
    void cancel_my_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation("스타크", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "스타크")
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("과거 날짜로 예약 시도 시 400을 반환한다.")
    @Test
    void save_reservation_with_past_date() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = new HashMap<>();
        params.put("name", "타스");
        params.put("date", "2020-01-01");
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("타인 예약 변경 시 403을 반환한다.")
    @Test
    void update_other_users_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation("타스", LocalDate.of(2028, 5, 6), themeId, timeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "카야");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @DisplayName("지난 예약 변경 시 400을 반환한다.")
    @Test
    void update_past_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation("타스", LocalDate.of(2020, 1, 1), themeId, timeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "타스");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("존재하지 않는 예약 변경 시 404를 반환한다.")
    @Test
    void update_non_existing_reservation() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Map<String, String> params = new HashMap<>();
        params.put("name", "타스");
        params.put("date", "2028-05-07");
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", 999)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("중복된 날짜/시간으로 예약 변경 시 409를 반환한다.")
    @Test
    void update_reservation_with_duplicate() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservation("타스", LocalDate.of(2028, 5, 6), themeId, timeId);
        Long reservationId = testHelper.insertReservation("카야", LocalDate.of(2028, 5, 7), themeId, timeId);

        Map<String, String> params = new HashMap<>();
        params.put("name", "카야");
        params.put("date", "2028-05-06");
        params.put("timeId", String.valueOf(timeId));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().patch("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @DisplayName("타인 예약 취소 시 403을 반환한다.")
    @Test
    void cancel_other_users_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation("타스", LocalDate.of(2028, 5, 6), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "카야")
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @DisplayName("지난 예약 취소 시 400을 반환한다.")
    @Test
    void cancel_past_reservation() {
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long reservationId = testHelper.insertReservation("타스", LocalDate.of(2020, 1, 1), themeId, timeId);

        RestAssured.given()
                .queryParam("name", "타스")
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("존재하지 않는 예약 취소 시 404를 반환한다.")
    @Test
    void cancel_non_existing_reservation() {
        RestAssured.given()
                .queryParam("name", "타스")
                .when().delete("/reservations/{id}", 999)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
