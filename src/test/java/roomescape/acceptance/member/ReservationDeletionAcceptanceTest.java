package roomescape.acceptance.member;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.customerToken;
import static roomescape.acceptance.PreInsertedData.RESERVATION_WAITING_CUSTOMER1_THEME3_240502_1200;
import static roomescape.acceptance.PreInsertedData.RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100;

@DisplayName("고객이 예약 대기를 삭제한다.")
class ReservationDeletionAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("정상 작동 - 고객이 자신의 예약 대기를 삭제한다.")
    @Test
    void deleteMyReservationWaiting_success() {
        sendDeleteRequest(RESERVATION_WAITING_CUSTOMER1_THEME3_240502_1200.getId())
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("예외 발생 고객이 자신의 예약 대기를 삭제한다.")
    @Test
    void deleteOthersReservationWaiting_fail() {
        CustomExceptionResponse response = sendDeleteRequest(RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100.getId())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract().as(CustomExceptionResponse.class);

        assertAll(
                () -> assertThat(response.title()).isEqualTo("작업을 수행할 권한이 없습니다."),
                () -> assertThat(response.detail()).isEqualTo("예약 삭제 권한이 없습니다.")
        );
    }

    private ValidatableResponse sendDeleteRequest(Long id) {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", customerToken)
                .when().delete("/reservations/" + id)
                .then().log().all();
    }
}
