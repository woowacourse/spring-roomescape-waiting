package roomescape.acceptance.member;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.PreInsertedData.CUSTOMER_1;
import static roomescape.PreInsertedData.CUSTOMER_2;
import static roomescape.PreInsertedData.THEME_1;
import static roomescape.PreInsertedData.TIME_10_O0;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.Fixture.customer1Token;
import static roomescape.domain.Reservation.Status;

class ReservationCreationAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("고객이 예약을 추가한다.")
    @Nested
    class addReservation {

        @DisplayName("정상 작동 - 예약을 추가한다.")
        @Test
        void addReservation_success() {
            MemberReservationRequest requestBody = makeReservationOn(
                    LocalDate.parse("2099-01-11")
            );

            ReservationResponse response = sendPostRequest(requestBody)
                    .statusCode(HttpStatus.CREATED.value())
                    .header("location", containsString("/reservations/"))
                    .extract().as(ReservationResponse.class);

            assertAll(
                    () -> assertThat(response.member().id()).isEqualTo(CUSTOMER_1.getId()),
                    () -> assertThat(response.status()).isEqualTo(Status.RESERVED)
            );
        }

        @DisplayName("정상 작동 - 다른 사용자의 예약이 있다면, 예약 대기를 추가한다.")
        @TestFactory
        Stream<DynamicTest> givenReservationAlreadyExistForDifferentCustomer_whenAddReservation_thenAddReservationWaiting() {
            AdminReservationRequest requestByAdmin = new AdminReservationRequest(
                    CUSTOMER_2.getId(),
                    LocalDate.parse("2099-12-31"),
                    1L,
                    1L
            );

            MemberReservationRequest request = new MemberReservationRequest(
                    LocalDate.parse("2099-12-31"),
                    1L,
                    1L
            );


            return Stream.of(
                    DynamicTest.dynamicTest("관리자가 고객2의 예약을 추가한다", () -> sendPostRequestByAdmin(requestByAdmin)),
                    DynamicTest.dynamicTest("고객1이 동일한 예약을 추가한다", () -> {
                                ReservationResponse response = sendPostRequest(request)
                                        .statusCode(HttpStatus.CREATED.value())
                                        .extract().as(ReservationResponse.class);
                                assertAll(
                                        () -> assertThat(response.member().id()).isEqualTo(CUSTOMER_1.getId()),
                                        () -> assertThat(response.status()).isEqualTo(Status.WAITING)
                                );
                            }
                    )
            );
        }

        @DisplayName("예외 발생 - 과거 시간에 대한 예약 추가한다.")
        @Test
        void addReservation_forPastTime_fail() {
            MemberReservationRequest reservationForPast = makeReservationOn(
                    LocalDate.now().minusDays(1)
            );

            CustomExceptionResponse response = sendPostRequest(reservationForPast)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(CustomExceptionResponse.class);

            assertAll(
                    () -> assertThat(response.title()).contains("허용되지 않는 작업입니다."),
                    () -> assertThat(response.detail()).contains("지나간 시간에 대한 예약은 할 수 없습니다.")
            );
        }

        @DisplayName("예외 발생 - 같은 사용자의 예약을 추가한다.")
        @TestFactory
        Stream<DynamicTest> givenReservationAlreadyExistForSameCustomer_whenAddReservation_thenFail() {
            AdminReservationRequest requestByAdmin = new AdminReservationRequest(
                    CUSTOMER_1.getId(),
                    LocalDate.parse("2099-12-31"),
                    1L,
                    1L
            );

            MemberReservationRequest request = new MemberReservationRequest(
                    LocalDate.parse("2099-12-31"),
                    1L,
                    1L
            );


            return Stream.of(
                    DynamicTest.dynamicTest("관리자가 고객1의 예약을 추가한다", () -> sendPostRequestByAdmin(requestByAdmin)),
                    DynamicTest.dynamicTest("고객1이 동일한 예약을 추가한다", () -> {
                        CustomExceptionResponse response = sendPostRequest(request)
                                        .statusCode(HttpStatus.BAD_REQUEST.value())
                                        .extract().as(CustomExceptionResponse.class);
                                assertAll(
                                        () -> assertThat(response.title()).isEqualTo("허용되지 않는 작업입니다."),
                                        () -> assertThat(response.detail()).isEqualTo("중복된 예약은 할 수 없습니다.")
                                );
                            }
                    )
            );
        }
    }

    private MemberReservationRequest makeReservationOn(LocalDate date) {
        return new MemberReservationRequest(
                date,
                TIME_10_O0.getId(),
                THEME_1.getId()
        );
    }

    private ValidatableResponse sendPostRequest(MemberReservationRequest requestBody) {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", customer1Token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all();
    }

    private ValidatableResponse sendPostRequestByAdmin(AdminReservationRequest requestBody) {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/admin/reservations")
                .then().log().all();
    }
}
