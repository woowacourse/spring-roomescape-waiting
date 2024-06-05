package roomescape.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import static roomescape.acceptance.Fixture.PRE_INSERTED_CUSTOMER_1;
import static roomescape.acceptance.Fixture.PRE_INSERTED_RESERVATION_1;
import static roomescape.acceptance.Fixture.PRE_INSERTED_RESERVATION_TIME_1;
import static roomescape.acceptance.Fixture.PRE_INSERTED_THEME_1;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.exception.RoomescapeExceptionCode.INVALID_DATETIME;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_ALREADY_EXISTS;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_NOT_FOUND;
import static roomescape.util.CookieUtil.TOKEN_NAME;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.ExceptionResponse;

class ReservationAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 예약 목록을 조회한다.")
    @Test
    void getReservations_success() {
        TypeRef<List<ReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie(TOKEN_NAME, adminToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(reservationListFormat);
    }

    @DisplayName("관리자가 예약을 추가한다.")
    @Nested
    class addReservation extends NestedAcceptanceTest {

        @DisplayName("정상 작동")
        @Test
        void addReservation_success() {
            ReservationRequest requestBody = getRequestBody(
                    LocalDate.parse("2099-12-30")
            );

            RestAssured.given().log().all()
                    .cookie(TOKEN_NAME, adminToken)
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when().post("/admin/reservations")
                    .then().log().all()
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
                    LocalDate.parse("2099-12-31")
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
                    PRE_INSERTED_CUSTOMER_1.getId(),
                    date,
                    PRE_INSERTED_RESERVATION_TIME_1.getId(),
                    PRE_INSERTED_THEME_1.getId()
            );
        }

        private ValidatableResponse sendPostRequest(ReservationRequest requestBody) {
            return RestAssured.given().log().all()
                    .cookie(TOKEN_NAME, adminToken)
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when().post("/admin/reservations")
                    .then().log().all();
        }
    }

    @DisplayName("관리자가 예약을 삭제한다.")
    @Nested
    class deleteReservation extends NestedAcceptanceTest {

        @DisplayName("정상 작동")
        @Test
        void deleteReservation_forExist_success() {
            Long existReservationId = PRE_INSERTED_RESERVATION_1.getId();

            sendDeleteRequest(existReservationId)
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("예외 발생 - 존재하지 않는 예약을 삭제한다.")
        @Test
        void deleteReservation_forNonExist_fail() {
            long notExistReservationId = 0L;

            ExceptionResponse response = sendDeleteRequest(notExistReservationId)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().as(ExceptionResponse.class);

            assertThat(response.message()).contains(RESERVATION_NOT_FOUND.message());
        }

        private ValidatableResponse sendDeleteRequest(Long id) {
            return RestAssured.given().log().all()
                    .cookie(TOKEN_NAME, adminToken)
                    .when().delete("/admin/reservations/" + id)
                    .then().log().all();
        }
    }
}
