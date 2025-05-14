package roomescape.admin.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import roomescape.dto.request.LoginRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminControllerTest {

    @Test
    void url을_기반으로_html을_요청받을_수_있다() {
        LoginRequest request = new LoginRequest(
                "admin@admin.com",
                "admin"
        );

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract();

        assertAll(() -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.asString()).contains("<title>방탈출 어드민</title>");
        });
    }

    @Test
    void 어드민이_아니라면_접근할_수_없다() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "test"
        );

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        RestAssured.given()
                .log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
