package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = this.port;
    }

    @DisplayName("날짜가 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_date() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", null);
        reservation.put("timeId", 1);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 날짜는 null 일 수 없습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 날짜로 생성 요청 시 400 응답을 준다.")
    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "123", "2월 5일", "2014년 2월 5일", "2023:12:03", "2024-15-10"})
    void when_given_wrong_date(final String date) {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", date);
        reservation.put("timeId", 1);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 요청 날짜 형식이 맞지 않습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 예약 시간 번호로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_wrong_time_id() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2024-12-03");
        reservation.put("timeId", "a");
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 요청 입력이 잘못되었습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 시간 번호가 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_time_id() {
        // given
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2024-12-03");
        reservation.put("timeId", null);
        ExceptionResponse expected = new ExceptionResponse("[ERROR] 예약 시간 번호는 null 일 수 없습니다.", "/reservations");
        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();
        // then
        ExceptionResponse actual = response.as(ExceptionResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 예약 id로 삭제 요청 시 400 응답을 준다.")
    @Test
    void when_given_wrong_id() {
        RestAssured.given().log().all()
                .when().delete("/reservations/2")
                .then().log().all()
                .statusCode(400);
    }
}
