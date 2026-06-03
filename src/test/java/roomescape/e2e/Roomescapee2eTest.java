package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class Roomescapee2eTest {

    @LocalServerPort
    int port;

    String user1Session;
    String user3Session;
    String futureDate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        futureDate = LocalDate.now().plusDays(12).toString();

        user1Session = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user1@test.com", "password", "1234"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().cookie("JSESSIONID");

        user3Session = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "user3@test.com", "password", "1234"))
                .when().post("/login")
                .then().statusCode(200)
                .extract().cookie("JSESSIONID");
    }

    @Test
    @DisplayName("대기 생성 -> 조회 -> 순번 확인")
    void 대기_순번_확인_테스트() {
        RestAssured.given().log().all()
                .cookie("JSESSIONID", user3Session)
                .contentType(ContentType.JSON)
                .body(Map.of("date", futureDate, "timeId", 2, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", user3Session)
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[1].turn", equalTo(2));

        RestAssured.given().log().all()
                .cookie("JSESSIONID", user1Session)
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].turn", equalTo(1));
    }

    @Test
    @DisplayName("대기 취소 후 순번 재정렬")
    void 순번_정렬_테스트() {
        RestAssured.given().log().all()
                .cookie("JSESSIONID", user3Session)
                .contentType(ContentType.JSON)
                .body(Map.of("date", futureDate, "timeId", 2, "themeId", 1))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", user1Session)
                .when().delete("/waitings/2")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", user3Session)
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[1].turn", equalTo(1));
    }
}
