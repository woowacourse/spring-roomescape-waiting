package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStep1Test {

    @LocalServerPort
    int port;

    String sessionId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        sessionId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "1234"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().cookie("JSESSIONID");
    }

    @Test
    void 예약_가능_시간_정상_흐름() {
        RestAssured.given().log().all()
                .when().get("/times/available?date=2099-08-05&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));

        RestAssured.given().log().all()
                .cookie("JSESSIONID", sessionId)
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2099-08-05", "timeId", 1, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/available?date=2099-08-05&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void 인기_테마_조회() {
        RestAssured.given().log().all()
                .when().get("/themes/top/3")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].name", is("테마A"))
                .body("[1].name", is("테마B"))
                .body("[2].name", is("테마C"));
    }
}