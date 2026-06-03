package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("회원가입한 사용자는 로그인하면 토큰을 받는다")
    void registeredUserReceivesTokenOnLogin() {
        register("brown@test.com", "pw", "브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "brown@test.com", "password", "pw"))
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401과 메시지를 반환한다")
    void returns401WhenPasswordIsWrong() {
        register("brown@test.com", "pw", "브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "brown@test.com", "password", "wrong"))
                .when().post("/login")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("INVALID_LOGIN"));
    }

    @Test
    @DisplayName("토큰이 없으면 보호된 API는 401을 반환한다")
    void protectedApiReturns401WithoutToken() {
        RestAssured.given().log().all()
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("로그인한 사용자는 본인 예약을 조회할 수 있다")
    void loggedInUserCanQueryOwnReservations() {
        register("brown@test.com", "pw", "브라운");

        RestAssured.given().log().all()
                .header("Authorization", DbFixtures.memberBearer(jdbcTemplate, "brown"))
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", equalTo(0));
    }

    @Test
    @DisplayName("관리자 API는 MANAGER만 접근할 수 있다")
    void adminApiIsAccessibleOnlyByManager() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .header("Authorization", DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(403)
                .body("code", equalTo("UNAUTHORIZED"));

        RestAssured.given().log().all()
                .header("Authorization", DbFixtures.managerBearer(jdbcTemplate, "관리자"))
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200);
    }

    private void register(String username, String password, String name) {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password, "name", name))
                .when().post("/users")
                .then().statusCode(201);
    }
}
