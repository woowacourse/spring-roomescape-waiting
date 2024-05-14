package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClientReservationTest {
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("날짜와 테마를 선택하면 예약 가능한 시간을 확인할 수 있다.")
    @Test
    void given_dateThemeId_when_books_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .when().get("/books/2099-04-30/1")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("사용자 예약 시 부적절한 입력값이 들어 올 경우 400오류를 반환한다.")
    @ParameterizedTest
    @CsvSource({"2099-01-11,test", "1111-22-33,1", "1111-22-33,test", ","})
    void given_when_booksWithInvalidDateAndThemeId_then_statusCodeIsBadRequest(String invalidDate, String invalidThemeId) {
        RestAssured.given().log().all()
                .when().get("/books/%s/%s".formatted(invalidDate, invalidThemeId))
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약이 정상적으로 성공한다.")
    @Test
    void given_reservationRequest_when_create_statusCodeIsOk() {
        //given
        Map<String, Object> login = new HashMap<>();
        login.put("email", "wedge@test.com");
        login.put("password", "test1234");
        var cookies = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(login)
                .when().post("/login")
                .then().log().all()
                .extract().response().getDetailedCookies();
        Map<String, String> params = new HashMap<>();
        params.put("date", "2099-01-01");
        params.put("themeId", "1");
        params.put("timeId", "2");
        //when, then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .cookies(cookies)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
