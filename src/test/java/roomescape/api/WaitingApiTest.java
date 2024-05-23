package roomescape.api;

import static org.hamcrest.Matchers.is;
import static roomescape.LoginTestSetting.getCookieByLogin;

import java.time.LocalDate;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.waiting.UserWaitingRequest;
import roomescape.infrastructure.auth.JwtProvider;

@Sql("/waiting-api-test-data.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingApiTest {

    @Autowired
    JwtProvider jwtProvider;

    @LocalServerPort
    int port;

    @Test
    void 전체_예약_대기_조회() {
        UserWaitingRequest waitingRequest = createUserWaitingRequest(1L, 1L);
        addWaiting(waitingRequest);

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 예약_대기_추가() {
        Cookie cookieByUserLogin = getCookieByLogin(port, "test1@email.com", "123456");
        UserWaitingRequest waitingRequest = createUserWaitingRequest(1L, 1L);

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByUserLogin)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/waitings/1");
    }

    @Test
    void 예약_대기_취소() {
        Cookie cookieByUserLogin = getCookieByLogin(port, "test1@email.com", "123456");
        UserWaitingRequest waitingRequest = createUserWaitingRequest(1L, 1L);
        addWaiting(waitingRequest);

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByUserLogin)
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 관리자_대기_거절() {
        Cookie cookieByUserLogin = getCookieByLogin(port, "admin@email.com", "123456");
        UserWaitingRequest waitingRequest = createUserWaitingRequest(1L, 1L);
        addWaiting(waitingRequest);

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByUserLogin)
                .when().delete("/admin/waitings/1")
                .then().log().all()
                .statusCode(204);
    }

    private UserWaitingRequest createUserWaitingRequest(Long timeId, Long themeId) {
        return new UserWaitingRequest(LocalDate.now().plusDays(1), timeId, themeId);
    }

    private void addWaiting(UserWaitingRequest waitingRequest) {
        Cookie cookieByUserLogin = getCookieByLogin(port, "test1@email.com", "123456");

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie(cookieByUserLogin)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);
    }
}
