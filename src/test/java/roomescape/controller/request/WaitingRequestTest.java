package roomescape.controller.request;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingRequestTest {

    @DisplayName("요청된 데이터의 날짜가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date() {
        WaitingRequest request = new WaitingRequest(null, 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 날짜가 과거일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date_past() {
        WaitingRequest request = new WaitingRequest(LocalDate.now().minusDays(1), 1L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 시간 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId() {
        WaitingRequest request = new WaitingRequest(LocalDate.now(), null, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 시간 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId_range() {
        WaitingRequest request = new WaitingRequest(LocalDate.now(), 0L, 1L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 테마 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId() {
        WaitingRequest request = new WaitingRequest(LocalDate.now(), 1L, null);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("요청된 데이터의 테마 id가 1 미만일 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId_range() {
        WaitingRequest request = new WaitingRequest(LocalDate.now(), 1L, 0L);
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(400);
    }
}
