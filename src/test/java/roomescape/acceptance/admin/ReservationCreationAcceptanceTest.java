package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.request.AdminReservationRequest;
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

@DisplayName("관리자가 예약을 추가한다.")
class ReservationCreationAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("정상 작동 - 예약을 추가한다.")
    @Test
    void addReservation_success() {
        AdminReservationRequest requestBody = getRequestBody(
                LocalDate.parse("2099-12-30")
        );

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("location", containsString("/reservations/"))
                .extract().as(ReservationResponse.class);
    }

    @DisplayName("정상 작동 - 다른 사용자의 예약이 있다면, 예약 대기를 추가한다.")
    @TestFactory
    Stream<DynamicTest> givenReservationAlreadyExistForDifferentCustomer_whenAddReservation_thenAddReservationWaiting() {
        AdminReservationRequest requestBody1 = new AdminReservationRequest(
                CUSTOMER_1.getId(),
                LocalDate.parse("2099-12-31"),
                1L,
                1L
        );

        AdminReservationRequest requestBody2 = new AdminReservationRequest(
                CUSTOMER_2.getId(),
                LocalDate.parse("2099-12-31"),
                1L,
                1L
        );

        return Stream.of(
                DynamicTest.dynamicTest("예약을 추가한다", () -> sendPostRequest(requestBody1)),

                DynamicTest.dynamicTest("다른 사용자의 동일한 예약을 추가한다", () -> {
                            ReservationResponse response = sendPostRequest(requestBody2)
                                    .statusCode(HttpStatus.CREATED.value())
                                    .extract().as(ReservationResponse.class);
                            assertAll(
                                    () -> assertThat(response.member().id()).isEqualTo(CUSTOMER_2.getId()),
                                    () -> assertThat(response.reservationStatus().isWaiting()).isTrue()
                            );
                        }
                )
        );
    }

    @DisplayName("예외 발생 - 과거 시간에 대한 예약 추가한다.")
    @Test
    void addReservation_forPastTime_fail() {
        AdminReservationRequest reservationForPast = getRequestBody(
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
        AdminReservationRequest request = new AdminReservationRequest(
                CUSTOMER_1.getId(),
                LocalDate.parse("2099-12-31"),
                1L,
                1L
        );

        return Stream.of(
                DynamicTest.dynamicTest("관리자가 예약을 추가한다", () -> sendPostRequest(request)),
                DynamicTest.dynamicTest("동일한 고객의 동일한 예약을 추가한다", () -> {
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

    private AdminReservationRequest getRequestBody(LocalDate date) {
        return new AdminReservationRequest(
                CUSTOMER_1.getId(),
                date,
                TIME_10_O0.getId(),
                THEME_1.getId()
        );
    }

    private ValidatableResponse sendPostRequest(AdminReservationRequest requestBody) {
        return RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/admin/reservations")
                .then().log().all();
    }
}
