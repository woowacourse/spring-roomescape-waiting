package roomescape;

import static org.hamcrest.CoreMatchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminEndToEndTest {

    @LocalServerPort
    int port;

    String accessToken;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = port;

        accessToken = RestAssured
                .given().log().all()
                .body(new MemberLoginRequest("redddy@gmail.com", "0000"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("시간 저장 및 삭제")
    void saveAndDeleteTime() {
        final Map<String, String> params = Map.of("startAt", "10:00");

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(6));

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/admin/times/6")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/admin/times/6")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("테마 저장 및 삭제")
    void saveAndDeleteTheme() {
        final Map<String, String> params = Map.of("name", "v1", "description", "blah", "thumbnail",
                "dkdk");

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/admin/themes/5")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/admin/themes/5")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 저장 및 삭제")
    void saveAndDeleteReservation() {
        final Map<String, String> params = Map.of(
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", "1",
                "themeId", "1");

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(7));

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/reservations/9")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/reservations/9")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("로그인 페이지를 반환")
    void showLoginPage() {
        RestAssured.given().log().all()
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }
}
