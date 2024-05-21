package roomescape.controller.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.BaseControllerTest;
import roomescape.controller.exception.ErrorResponse;

@Sql("/member.sql")
class AdminPageControllerTest extends BaseControllerTest {

    @ParameterizedTest(name = "{0} 페이지를 조회한다.")
    @ValueSource(strings = {
            "/admin",
            "/admin/reservation",
            "/admin/time",
            "/admin/theme",
            "/admin/waiting"
    })
    void pageTest(String path) {
        adminLogin();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get(path)
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("로그인하지 않으면 401 에러가 발생한다.")
    void notLoggedIn() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/admin")
                .then().log().all()
                .extract();

        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.message()).isEqualTo("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("로그인을 했지만 어드민이 아니면 403 에러가 발생한다.")
    void notAdmin() {
        userLogin();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .extract();

        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(errorResponse.message()).isEqualTo("어드민 권한이 필요합니다.");
    }
}
