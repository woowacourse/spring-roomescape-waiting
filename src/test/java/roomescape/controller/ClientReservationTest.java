package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.AuthorizationExtractor;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClientReservationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestAccessToken testAccessToken;

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
    void given_when_booksWithInvalidDateAndThemeId_then_statusCodeIsBadRequest(String invalidDate,
                                                                               String invalidThemeId) {
        RestAssured.given().log().all()
                .when().get("/books/%s/%s".formatted(invalidDate, invalidThemeId))
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약이 정상적으로 성공한다.")
    @Test
    void given_reservationRequest_when_create_statusCodeIsCreated() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("date", "2099-01-01");
        params.put("themeId", "1");
        params.put("timeId", "2");
        //when, then
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("로그인한 사용자의 예약을 조회한다.")
    @Test
    void given_memberToken_when_readByMember_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("사용자 예약 등록 시 부적절한 값이 들어올 경우 400오류를 반환한다.")
    @Test
    void given_memberToken_when_createWithInvalidParam_then_statusCodeIsBadRequest() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("date", "2099-01-01");
        params.put("themeId", "1");
        params.put("timeId", "-2");
        //when, then
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약 등록 시 과거의 시간이 들어올 경우 400오류를 반환한다.")
    @Test
    void given_memberToken_when_createWithPastDate_then_statusCodeIsBadRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("date", "1999-01-01");
        params.put("theme", "3");
        params.put("time", "2");
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약 등록 시 이미 같은 시간대에 예약이 등록되어 있을 경우 400 오류 반환")
    @Test
    void given_memberToken_when_createWithAlreadyReserved_then_statusCodeIsBadRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("date", "2099-04-30");
        params.put("themeId", "1");
        params.put("timeId", "1");
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("다른 사용자가 이미 예약된 시간에 예약을 할 경우 예약이 성공한다.")
    @Test
    void given_differentMemberToken_when_createWaitWithAlreadyReserved_then_statusCodeIsCreated() {
        Map<String, String> params = new HashMap<>();
        params.put("date", "2099-04-30");
        params.put("themeId", "1");
        params.put("timeId", "1");
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken("user2@test.com"))
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("로그인한 사용자의 대기중인 예약을 삭제한다.")
    @Test
    void given_memberToken_when_deleteWaiting_then_statusCodeIsNoContent() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .when().delete("/reservations-mine/wait/10")
                .then().log().all()
                .statusCode(204);
    }
}
