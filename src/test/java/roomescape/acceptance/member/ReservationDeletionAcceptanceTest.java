package roomescape.acceptance.member;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.MyReservationResponse;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.PreInsertedData.RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100;
import static roomescape.PreInsertedData.RESERVATION_WAITING_CUSTOMER3_THEME2_240501_1100;
import static roomescape.acceptance.Fixture.customer1Token;
import static roomescape.acceptance.Fixture.customer2Token;
import static roomescape.acceptance.Fixture.customer3Token;

@DisplayName("고객이 예약 대기를 삭제한다.")
class ReservationDeletionAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("정상 작동 - 고객이 예약 대기를 삭제해서 그 다음 대기의 순서가 당겨진다.")
    @TestFactory
    Stream<DynamicTest> deleteMyReservationWaiting_success() {
        Reservation reservation = RESERVATION_WAITING_CUSTOMER3_THEME2_240501_1100;

        return Stream.of(
                DynamicTest.dynamicTest("고객3은 두번째로 대기한다.", () -> {
                    MyReservationResponse myReservationResponse = sendRequestToGetWaitingStatus(reservation, customer3Token);

                    assertThat(myReservationResponse.waiting().reservationStatus()).isEqualTo(ReservationStatus.WAITING);
                    assertThat(myReservationResponse.waiting().waitingRank()).isEqualTo(2L);
                }),

                DynamicTest.dynamicTest("고객2가 예약 대기를 삭제한다.", () -> {
                    sendDeleteRequest(RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100.getId(), customer2Token);
                }),

                DynamicTest.dynamicTest("고객3은 첫번째로 대기한다.", () -> {
                    MyReservationResponse myReservationResponse = sendRequestToGetWaitingStatus(reservation, customer3Token);

                    assertThat(myReservationResponse.waiting().reservationStatus()).isEqualTo(ReservationStatus.WAITING);
                    assertThat(myReservationResponse.waiting().waitingRank()).isEqualTo(1L);
                })
        );
    }

    private MyReservationResponse sendRequestToGetWaitingStatus(Reservation reservation, String token) {
        TypeRef<MultipleResponse<MyReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        MultipleResponse<MyReservationResponse> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .extract().as(reservationListFormat);

        return response.items().stream().filter(r -> r.date().equals(reservation.getDate())
                                && r.time().id().equals(reservation.getReservationTime().getId())
                                && r.theme().id().equals(reservation.getTheme().getId()))
                .findAny().get();
    }

    @DisplayName("예외 발생 - 고객이 자신의 것이 아닌 예약 대기를 삭제한다.")
    @Test
    void deleteOthersReservationWaiting_fail() {
        CustomExceptionResponse response = sendDeleteRequest(RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100.getId(), customer1Token)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract().as(CustomExceptionResponse.class);

        assertAll(
                () -> assertThat(response.title()).isEqualTo("작업을 수행할 권한이 없습니다."),
                () -> assertThat(response.detail()).isEqualTo("예약 삭제 권한이 없습니다.")
        );
    }

    private ValidatableResponse sendDeleteRequest(Long id, String token) {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", token)
                .when().delete("/reservations/" + id)
                .then().log().all();
    }
}
