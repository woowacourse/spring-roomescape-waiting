package roomescape.controller.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(value = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserPageControllerTest {

    private String userToken;

    @BeforeEach
    void login() {
        LoginRequest user = new LoginRequest("user@a.com", "123a!");

        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("메인 페이지 응답 -> 200")
    @Test
    void getMainPage() {
        RestAssured.given().log().all()
            .when().get("/")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("/reservation 페이지 응답 -> 200")
    @Test
    void getReservationPage() {
        RestAssured.given().log().all()
            .cookie("token", userToken)
            .when().get("/reservation")
            .then().log().all()
            .statusCode(200);
    }
}
