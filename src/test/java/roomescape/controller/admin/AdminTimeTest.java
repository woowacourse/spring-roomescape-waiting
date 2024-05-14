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
public class AdminTimeTest {
    private static final String ADMIN_USER = "wedge@test.com";

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


    @DisplayName("시간 등록 성공 시 201을 응답한다.")
    @Test
    void given_timeResult_when_saveAndDeleteTimes_then_statusCodeIsCreated() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:10");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("time 삭제 성공 시 204를 응답한다.")
    @Test
    void given_when_deleteTimes_then_statusCodeNoContents() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .when().delete("/admin/times/4")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("이미 등록된 시간을 등록하면 400 오류를 반환한다.")
    @Test
    void given_when_saveDuplicatedTime_then_statusCodeIsBadRequest() {
        Map<String, Object> time = new HashMap<>();
        time.put("startAt", "10:00");

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("이미 등록된 시간입니다"));
    }

    @DisplayName("비어있는 시간으로 등록하는 경우 400 오류를 반환한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", ""})
    void given_when_saveInvalidTime_then_statusCodeIsBadRequest(String invalidTime) {
        Map<String, Object> time = new HashMap<>();
        time.put("startAt", invalidTime);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("startAt"));
    }

    @DisplayName("부적절한 양식으로 시간을 등록하는 경우 개발자가 정의한 문구로 400 오류를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"99:99", "aaaa"})
    @NullSource
    void given_when_saveInvalidTime_then_statusCodeIsBadRequestWithCustomMessage(String invalidTime) {
        Map<String, Object> time = new HashMap<>();
        time.put("startAt", invalidTime);

        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body(containsString("startAt"));
    }

    @DisplayName("삭제하고자 하는 시간에 예약이 등록되어 있으면 400 오류를 반환한다.")
    @Test
    void given_when_deleteTimeIdRegisteredReservation_then_statusCodeIsBadRequest() {
        RestAssured.given().log().all()
                .cookie(AuthorizationExtractor.TOKEN_NAME, generateToken(ADMIN_USER))
                .contentType(ContentType.JSON)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(400)
                .body(containsString("예약이 등록된 시간은 제거할 수 없습니다"));
    }
}
