package roomescape.controller.admin;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.TestDataInitExtension;
import roomescape.auth.AuthorizationExtractor;
import roomescape.controller.TestAccessToken;

/*
 * 예약 대기 관련 초기 데이터
 * 예약 테이블
 * {ID=9, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
 * {ID=10, DATE=내일일자, TIME_ID=2, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
 * 예약 대기 테이블
 * {ID=1, DATE='2024-04-30', TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=3, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=3, STATUS=WAITING}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestDataInitExtension.class)
class AdminReservationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestAccessToken testAccessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("예약 목록을 불러오는데 성공하면 200을 응답한다.")
    @Test
    void given_when_GetReservations_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예약 등록 성공 시 201을 응답한다.")
    @Test
    void given_reservationRequest_when_saveSuccessful_then_statusCodeIsCreate() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2999-12-31");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약 삭제 성공 시 204를 응답한다.")
    @Test
    void given_when_deleteSuccessful_then_statusCodeIsNoContents() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예약 삭제 시 예약 대기가 있는 경우 예약으로 전환한다.")
    @Test
    void given_when_existWaiting_then_convertWaitingToReservation() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().delete("/admin/reservations/9")
                .then().log().all();

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .when().get("/admin/waitings")
                .then().log().all()
                .body("size()", is(2));
    }

    @DisplayName("등록되지 않은 시간으로 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveNotExistTimeId_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2099-01-01");
        reservation.put("timeId", 500);
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("부적절한 날짜로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", "", "2011-02-09"})
    void given_when_saveInvalidDate_then_statusCodeIsBadRequest(String invalidDate) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", invalidDate);
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("부적절한 시간으로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", "", "0", "-1"})
    void given_when_saveInvalidTimeId_then_statusCodeIsBadRequest(String invalidTimeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2999-12-31");
        reservation.put("timeId", invalidTimeId);
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("timeId"));
    }

    @DisplayName("지나간 날짜와 시간으로 예약할 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveWithPastReservation_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "1999-01-01");
        reservation.put("timeId", 1); // 10:00
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("지나간 날짜와 시간으로 예약할 수 없습니다"));
    }

    @DisplayName("이미 예약이 된 시간을 등록하려 하면 400 오류를 반환한다.")
    @Test
    void given_when_saveDuplicatedReservation_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2099-04-30");
        reservation.put("timeId", 1); // 10:00
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("이미 예약이 등록되어 있습니다."));
    }

    @DisplayName("부적절한 테마로 예약하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", ""})
    void given_when_saveInvalidThemeId_then_statusCodeIsBadRequest(String invalidThemeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2999-04-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", invalidThemeId);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body(containsString("themeId"));
    }

    @DisplayName("등록되지 않은 시간으로 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveNotExistThemeId_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", "2099-01-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", 99);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("등록되지 않은 회원으로 예약하는 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveNotExistMemberId_then_statusCodeIsBadRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2099-01-01");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("memberId", 99);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("이미 예약이 된 시간의 다른 테마를 예약을 할 수 있다.")
    @Test
    void given_when_saveDuplicatedReservationDateAndTimeAndDifferentThemeId_then_statusCodeIsCreated() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2099-12-30");
        reservation.put("timeId", 1); // 10:00
        reservation.put("themeId", 3);
        reservation.put("memberId", 1);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("일반 유저가 예약을 삭제하려 할때 401오류를 반환한다.")
    @Test
    void given_userCookie_when_deleteFail_then_statusCodeIsNoContents() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(401);
    }
}
