package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.PreInsertedData.RESERVATION_CUSTOMER1_THEME2_240501_1100;

class ReservationDeletionAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 예약을 삭제한다.")
    @Nested
    class deleteReservation extends NestedAcceptanceTest {

        @DisplayName("정상 작동")
        @Test
        void deleteReservation_forExist_success() {
            Long existReservationId = RESERVATION_CUSTOMER1_THEME2_240501_1100.getId();

            sendDeleteRequest(existReservationId)
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("예외 발생 - 존재하지 않는 예약을 삭제한다.")
        @Test
        void deleteReservation_forNonExist_fail() {
            long notExistReservationId = 0L;

            CustomExceptionResponse response = sendDeleteRequest(notExistReservationId)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().as(CustomExceptionResponse.class);

            assertAll(
                    () -> assertThat(response.title()).contains("리소스를 찾을 수 없습니다."),
                    () -> assertThat(response.detail()).contains("아이디에 해당하는 예약을 찾을 수 없습니다.")
            );
        }

        private ValidatableResponse sendDeleteRequest(Long id) {
            return RestAssured.given().log().all()
                    .cookie("token", adminToken)
                    .when().delete("/admin/reservations/" + id)
                    .then().log().all();
        }
    }
}
