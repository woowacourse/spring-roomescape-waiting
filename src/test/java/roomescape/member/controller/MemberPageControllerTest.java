package roomescape.member.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.common.config.IntegrationTest;

class MemberPageControllerTest extends IntegrationTest {

    @DisplayName("/signup을 요청하면 html을 반환한다.")
    @Test
    void userSignUpPage() {
        RestAssured.given().log().all()
                .when()
                .get("/signup")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @DisplayName("/login을 요청하면 html을 반환한다.")
    @Test
    void userLoginPage() {
        RestAssured.given().log().all()
                .when()
                .get("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @DisplayName("/reservation을 요청하면 html을 반환한다.")
    @Test
    void userReservationPage() {
        RestAssured.given().log().all()
                .when()
                .get("/reservation")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @DisplayName("/reservation-mine을 요청하면 html을 반환한다.")
    @Test
    void myReservationPage() {
        RestAssured.given().log().all()
                .when()
                .get("/reservation-mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @DisplayName("/을 요청하면 html을 반환한다.")
    @Test
    void popularPage() {
        RestAssured.given().log().all()
                .when()
                .get("/")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }
}
