package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.MyReservationResponse;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.*;
import static roomescape.acceptance.PreInsertedData.*;

class ReservationDeletionAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 예약을 삭제한다.")
    @Nested
    class deleteReservation extends NestedAcceptanceTest {

        @DisplayName("정상 작동 - 관리자가 예약을 삭제하고, 대기가 없기 때문에 대기 순서가 바뀌지 않는다.")
        @Test
        void deleteReservation_forExist_success2() { //todo: gpt 한테 이름 물어보기
            sendDeleteRequest(RESERVATION_CUSTOMER2_THEME3_240503_1200.getId())
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("정상 작동 - 관리자가 예약을 삭제해서 그 다음 대기자가 예약되고, 예약 대기 순서가 바뀐다.")
        @TestFactory
        Stream<DynamicTest> deleteReservation_forExist_success() {
            Reservation reservation = RESERVATION_CUSTOMER1_THEME2_240501_1100;

            return Stream.of(
                    DynamicTest.dynamicTest("고객2는 첫번째로 대기한다.", () -> {
                        MyReservationResponse myReservationResponse = sendRequestToGetWaitingStatus(reservation, customer2Token);

                        assertThat(myReservationResponse.waiting().reservationStatus()).isEqualTo(ReservationStatus.WAITING);
                        assertThat(myReservationResponse.waiting().waitingRank()).isEqualTo(1L);
                    }),

                    DynamicTest.dynamicTest("고객3은 두번째로 대기한다.", () -> {
                        MyReservationResponse myReservationResponse = sendRequestToGetWaitingStatus(reservation, customer3Token);

                        assertThat(myReservationResponse.waiting().reservationStatus()).isEqualTo(ReservationStatus.WAITING);
                        assertThat(myReservationResponse.waiting().waitingRank()).isEqualTo(2L);
                    }),

                    DynamicTest.dynamicTest("관리자가 예약을 하나 삭제한다.", () -> {
                        sendDeleteRequest(RESERVATION_CUSTOMER1_THEME2_240501_1100.getId());
                    }),

                    DynamicTest.dynamicTest("고객2의 예약이 예약된 상태로 바뀐다.", () -> {
                        MyReservationResponse myReservationResponse = sendRequestToGetWaitingStatus(reservation, customer2Token);

                        assertThat(myReservationResponse.waiting().reservationStatus()).isEqualTo(ReservationStatus.RESERVED);
                    }),

                    DynamicTest.dynamicTest("고객3의 예약 대기가 첫번째로 바뀐다.", () -> {
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

            return response.items().stream()
                    .filter(r -> r.date().equals(reservation.getDate())
                            && r.time().id().equals(reservation.getReservationTime().getId())
                            && r.theme().id().equals(reservation.getTheme().getId()))
                    .findAny().get();
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
