package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.TestDataInitExtension;
import roomescape.auth.AuthorizationExtractor;
import roomescape.domain.dto.WaitingRequest;

/*
 * 예약 대기 관련 초기 데이터
 * 예약 테이블
 * {ID=9, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
 * {ID=10, DATE=내일일자, TIME_ID=2, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
 * 예약 대기 테이블
 * {ID=1, DATE='2024-04-30', TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(TestDataInitExtension.class)
class ClientWaitingControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestAccessToken testAccessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("해당 시간, 테마의 예약이 있으면 예약 대기를 할 수 있다.")
    void given_alreadyBooked_when_waiting_then_statusCodeIsCreated() {
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "2");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("동일한 시간의 예약 대기 내역이 있는 경우 예약 대기를 할 수 없다.")
    void given_alreadyWaiting_when_waiting_then_statusCodeIsBadRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("해당 시간, 테마의 예약이 없는 경우 예약 대기를 할 수 없다.")
    void given_notBooked_when_waiting_then_statusCodeIsBadRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "3");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getAdminToken())
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("일반 유저는 본인이 작성한 예약 대기 내역이 아니면 삭제할 수 없다.")
    void given_user_when_deleteOtherMembersWaiting_then_statusCodeIsBadRequest() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, testAccessToken.getUserToken())
                .when().delete("/waiting/2")
                .then().log().all()
                .statusCode(400);
    }
}
