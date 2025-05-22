package roomescape.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exception.handler.dto.ExceptionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationIntegrationTest {

    @DisplayName("날짜가 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_date() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", null);
        reservation.put("timeId", 1);

        ExceptionResponse expected = new ExceptionResponse(400, "[ERROR] 날짜는 null 일 수 없습니다.", "/reservations");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();

        ExceptionResponse actual = response.as(ExceptionResponse.class);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 날짜로 생성 요청 시 400 응답을 준다.")
    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "123", "2월 5일", "2014년 2월 5일", "2023:12:03", "2024-15-10"})
    void when_given_wrong_date(final String date) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", 1);

        ExceptionResponse expected = new ExceptionResponse(400, "[ERROR] 요청 날짜 형식이 맞지 않습니다.", "/reservations");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();

        ExceptionResponse actual = response.as(ExceptionResponse.class);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 예약 시간 번호로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_wrong_time_id() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2024-12-03");
        reservation.put("timeId", "a");

        ExceptionResponse expected = new ExceptionResponse(400, "[ERROR] 요청 입력이 잘못되었습니다.", "/reservations");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();

        ExceptionResponse actual = response.as(ExceptionResponse.class);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 시간 번호가 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_time_id() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2024-12-03");
        reservation.put("timeId", null);

        ExceptionResponse expected = new ExceptionResponse(400, "[ERROR] 예약 시간 번호는 null 일 수 없습니다.", "/reservations");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();

        ExceptionResponse actual = response.as(ExceptionResponse.class);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 예약 id로 삭제 요청 시 400 응답을 준다.")
    @Test
    void when_given_wrong_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/10")
                .then().log().all()
                .statusCode(400);
    }
}
