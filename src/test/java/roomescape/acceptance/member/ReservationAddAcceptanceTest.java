package roomescape.acceptance.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import static roomescape.acceptance.Fixture.PRE_INSERTED_RESERVATION_TIME_1;
import static roomescape.acceptance.Fixture.PRE_INSERTED_THEME_1;
import static roomescape.acceptance.Fixture.customerToken;
import static roomescape.exception.RoomescapeExceptionCode.INVALID_DATETIME;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_ALREADY_EXISTS;
import static roomescape.util.CookieUtil.TOKEN_NAME;

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
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.ExceptionResponse;

@DisplayName("고객이 예약을 추가한다.")
class ReservationAddAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("정상 작동")
    @Test
    void addReservation_success() {
        ReservationRequest requestBody = getRequestBody(
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
        ReservationRequest reservationForPast = getRequestBody(
                LocalDate.now().minusDays(1)
        );

        ExceptionResponse response = sendPostRequest(reservationForPast)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().as(ExceptionResponse.class);

        assertThat(response.message()).contains(INVALID_DATETIME.message());
    }

    @DisplayName("예외 발생 - 이미 있는 예약을 추가한다.")
    @TestFactory
    Stream<DynamicTest> addReservation_alreadyExist_fail() {
        ReservationRequest requestBody = getRequestBody(
                LocalDate.parse("2099-01-11")
        );

        return Stream.of(
                DynamicTest.dynamicTest("예약을 추가한다", () -> sendPostRequest(requestBody)),

                DynamicTest.dynamicTest("동일한 예약을 추가한다", () -> {
                            ExceptionResponse response = sendPostRequest(requestBody)
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .extract().as(ExceptionResponse.class);
                            assertThat(response.message()).contains(RESERVATION_ALREADY_EXISTS.message());
                        }
                )
        );
    }

    private ReservationRequest getRequestBody(LocalDate date) {
        return new ReservationRequest(
                null,
                date,
                PRE_INSERTED_RESERVATION_TIME_1.getId(),
                PRE_INSERTED_THEME_1.getId()
        );
    }

    private ValidatableResponse sendPostRequest(ReservationRequest requestBody) {
        return RestAssured.given().log().ifValidationFails()
                .cookie(TOKEN_NAME, customerToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all();
    }
}
