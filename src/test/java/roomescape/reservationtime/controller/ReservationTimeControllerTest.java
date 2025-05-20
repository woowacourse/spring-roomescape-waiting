package roomescape.reservationtime.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;

@Sql({"/data.sql"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeControllerTest {

    @Test
    void 예약_시간_조회() {
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }


    @Test
    void 예약_시간_저장() {
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        Map<String, String> time = Map.of("startAt", "13:00");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @Sql({"/reset.sql", "/member.sql", "/time.sql"})
    void 예약_시간_삭제() {
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약_시간이_포함된_예약이_있다면_삭제시도_시_예외_발생() {
        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(400);
    }
}
