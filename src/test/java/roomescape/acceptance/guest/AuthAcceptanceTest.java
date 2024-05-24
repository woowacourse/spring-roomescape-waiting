package roomescape.acceptance.guest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AuthAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("로그인하지 않은 사용자의 로그인 정보를 확인한다.")
    @Test
    void notLogin_andGetLoginInfo_fail() {
        CustomExceptionResponse response = RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().as(CustomExceptionResponse.class);

        assertAll(
                () -> assertThat(response.title()).isEqualTo("인증에 실패했습니다."),
                () -> assertThat(response.detail()).isEqualTo("토큰이 존재하지 않습니다.")
        );
    }

    @DisplayName("로그인하지 않은 사용자가 예약을 시도한다.")
    @Test
    void notLogin_tryReservation_fail() {
        CustomExceptionResponse response = RestAssured.given().log().all()
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().as(CustomExceptionResponse.class);

        assertAll(
                () -> assertThat(response.title()).isEqualTo("인증에 실패했습니다."),
                () -> assertThat(response.detail()).isEqualTo("토큰이 존재하지 않습니다.")
        );
    }
}
