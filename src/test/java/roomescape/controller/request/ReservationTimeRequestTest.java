package roomescape.controller.request;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeRequestTest {

    @DisplayName("요청된 시간이 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_startAt_is_null() {
        ReservationTimeRequest request = new ReservationTimeRequest(null);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 시간이 유효한 경우 예외가 발생하지 않는다.")
    @ParameterizedTest
    @ValueSource(strings = {"07:00", "23:59:59", "12:00:00", "00:00:00"})
    void should_not_throw_exception_when_startAt_is_good_format(String startAt) {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.parse(startAt));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }
}
