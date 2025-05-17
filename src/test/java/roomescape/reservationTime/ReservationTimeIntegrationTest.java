package roomescape.reservationTime;

import static org.hamcrest.CoreMatchers.is;

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
public class ReservationTimeIntegrationTest {

    @DisplayName("예약 시간을 조회할 수 있다.")
    @Test
    void reservation_time_view() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @DisplayName("예약 시간을 추가 및 삭제 할 수 있다.")
    @Test
    void reservation_time_post_to_add() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/times/4")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("시간이 null인 상태로 생성 요청 시 400 응답을 준다.")
    @Test
    void when_given_null_time() {
        Map<String, Object> reservationTime = new HashMap<>();
        reservationTime.put("startAt", null);

        ExceptionResponse expected = new ExceptionResponse(400, "[ERROR] time은 null 일 수 없습니다.", "/times");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();

        ExceptionResponse actual = response.as(ExceptionResponse.class);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("잘못된 시간으로 생성 요청 시 400 응답을 준다.")
    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "123", "2시5분", "2023:12:03", "10-39"})
    void when_given_wrong_time(final String time) {
        Map<String, Object> reservationTime = new HashMap<>();
        reservationTime.put("startAt", time);

        ExceptionResponse expected = new ExceptionResponse(400, "[ERROR] 요청 시간 형식이 맞지 않습니다.", "/times");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .extract()
                .response();

        ExceptionResponse actual = response.as(ExceptionResponse.class);
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
