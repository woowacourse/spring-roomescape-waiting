package roomescape.acceptance.guest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.request.MemberReservationRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.PreInsertedData.THEME_1;
import static roomescape.acceptance.PreInsertedData.TIME_10_O0;

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
        MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.parse("2099-12-31"),
                TIME_10_O0.getId(),
                THEME_1.getId()
        );

        CustomExceptionResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
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
