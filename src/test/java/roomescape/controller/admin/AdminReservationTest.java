package roomescape.controller.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.AuthorizationExtractor;
import roomescape.auth.JwtTokenProvider;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminReservationTest {
    private static final String ADMIN_USER = "wedge@test.com";
    private static final String COMMON_USER = "poke@test.com";

    @LocalServerPort
    int port;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String generateToken(String email) {
        return jwtTokenProvider.createToken(email);
    }

    @DisplayName("예약 목록을 불러오는데 성공하면 200을 응답한다.")
    @Test
    void given_when_GetReservations_then_statusCodeIsOk() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
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
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("일반 유저가 예약을 삭제하려 할때 401오류를 반환한다.")
    @Test
    void given_commonUserCookie_when_deleteFail_then_statusCodeIsNoContents() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(COMMON_USER))
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(401);
    }
}
