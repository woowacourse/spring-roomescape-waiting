package roomescape.acceptance.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;

import static roomescape.acceptance.Fixture.customerToken;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_TIME_1;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_THEME_1;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.response.ReservationResponse;

@DisplayName("고객이 예약을 추가한다.")
class ReservationAddAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("정상 작동")
    @Test
    void addReservation_success() {
        MemberReservationRequest requestBody = getRequestBody(
                LocalDate.parse("2099-01-11")
        );

        sendPostRequest(requestBody)
                .statusCode(HttpStatus.CREATED.value())
                .header("location", containsString("/reservations/"))
                .extract().as(ReservationResponse.class);
    }

    @DisplayName("예외 발생 - 과거 시간에 대한 예약 추가한다.")
    @Test
    void addReservation_forPastTime_fail() {
        MemberReservationRequest reservationForPast = getRequestBody(
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

    @DisplayName("예외 발생 - 이미 있는 예약을 추가한다.")
    @TestFactory
    Stream<DynamicTest> addReservation_alreadyExist_fail() {
        MemberReservationRequest requestBody = getRequestBody(
                LocalDate.parse("2099-01-11")
        );

        return Stream.of(
                DynamicTest.dynamicTest("예약을 추가한다", () -> sendPostRequest(requestBody)),

                DynamicTest.dynamicTest("동일한 예약을 추가한다", () -> {
                            CustomExceptionResponse response = sendPostRequest(requestBody)
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .extract().as(CustomExceptionResponse.class);
                            assertAll(
                                    () -> assertThat(response.title()).contains("허용되지 않는 작업입니다."),
                                    () -> assertThat(response.detail()).contains("예약이 이미 존재합니다.")
                            );

                        }
                )
        );
    }

    private MemberReservationRequest getRequestBody(LocalDate date) {
        return new MemberReservationRequest(
                date,
                PRE_INSERTED_RESERVATION_TIME_1.getId(),
                PRE_INSERTED_THEME_1.getId()
        );
    }

    private ValidatableResponse sendPostRequest(MemberReservationRequest requestBody) {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", customerToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all();
    }
}
