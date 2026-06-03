package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
    void 회원가입한_사용자는_로그인하면_토큰을_받는다() {
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
    void 비밀번호가_틀리면_401과_메시지를_반환한다() {
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
    void 토큰이_없으면_보호된_API는_401을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    @Test
    void 로그인한_사용자는_본인_예약을_조회할_수_있다() {
        register("brown@test.com", "pw", "브라운");

        RestAssured.given().log().all()
                .header("Authorization", DbFixtures.memberBearer(jdbcTemplate, "brown"))
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", equalTo(0));
    }

    @Test
    void 관리자_API는_MANAGER만_접근할_수_있다() {
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