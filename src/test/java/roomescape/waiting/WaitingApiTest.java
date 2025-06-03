package roomescape.waiting;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.api.LoginFixture;
import roomescape.reservation.waiting.dto.CreateUserWaitingRequest;
import roomescape.reservation.waiting.dto.CreateWaitingRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-time-data.sql", "/test-theme-data.sql", "/test-member-data.sql", "/test-reservation-data.sql",
        "/test-waiting-data.sql"})
public class WaitingApiTest {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @DisplayName("사용자 예약 대기 테스트")
    @Nested
    class User {

        private static final CreateUserWaitingRequest REQUEST = new CreateUserWaitingRequest(TOMORROW, 1L, 1L);
        private static String TOKEN;

        @LocalServerPort
        int port;

        @BeforeEach
        void setUp() {
            RestAssured.port = port;
            TOKEN = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(LoginFixture.userLoginRequest())
                    .when().post("/login")
                    .then().log().all()
                    .extract().cookie(TOKEN_COOKIE_NAME);
        }

        @DisplayName("예약 대기 생성을 성공할 경우 201을 반환한다.")
        @Test
        void testCreate() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(TOKEN_COOKIE_NAME, TOKEN)
                    .body(REQUEST)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", Matchers.equalTo(3))
                    .body("time.id", Matchers.equalTo(1))
                    .body("theme.id", Matchers.equalTo(1));
        }

        @DisplayName("예약 대기 삭제를 성공할 경우 204를 반환한다.")
        @Test
        void testDelete() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(TOKEN_COOKIE_NAME, TOKEN)
                    .when().delete("/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(204);
        }
    }

    @DisplayName("관리자 예약 대기 테스트")
    @Nested
    class Admin {

        private static final CreateWaitingRequest REQUEST = new CreateWaitingRequest(TOMORROW, 1L, 1L,
                LoginFixture.USER_ID);
        private static String TOKEN;

        @LocalServerPort
        int port;

        @BeforeEach
        void setUp() {
            RestAssured.port = port;
            TOKEN = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(LoginFixture.adminLoginRequest())
                    .when().post("/login")
                    .then().log().all()
                    .extract().cookie(TOKEN_COOKIE_NAME);
        }

        @DisplayName("예약 대기 승인을 성공할 경우 200을 반환한다.")
        @Test
        void testApprove() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(TOKEN_COOKIE_NAME, TOKEN)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(TOKEN_COOKIE_NAME, TOKEN)
                    .when().patch("/admin/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(200)
                    .body("id", Matchers.equalTo(4))
                    .body("time.id", Matchers.equalTo(1))
                    .body("theme.id", Matchers.equalTo(1));
        }

        @DisplayName("예약 대기 거절을 성공할 경우 204를 반환한다.")
        @Test
        void testDeny() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(TOKEN_COOKIE_NAME, TOKEN)
                    .when().delete("/admin/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(204);
        }
    }
}
