package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.PreInsertedData.*;

class ReservationAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 예약 목록을 조회한다.")
    @Test
    void getReservations_success() {
        TypeRef<List<ReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/reservations")
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

        @DisplayName("예외 발생 - 이미 있는 예약을 추가한다.")
        @TestFactory
        Stream<DynamicTest> addReservation_alreadyExist_fail() {
            AdminReservationRequest requestBody = getRequestBody(
                    LocalDate.parse("2099-12-31")
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

        private AdminReservationRequest getRequestBody(LocalDate date) {
            return new AdminReservationRequest(
                    PRE_INSERTED_CUSTOMER_1.getId(),
                    date,
                    PRE_INSERTED_RESERVATION_TIME_1.getId(),
                    PRE_INSERTED_THEME_1.getId()
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

        @DisplayName("예외 발생 - 존재하지 않는 예약 삭제한다.")
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
                    .when().delete("/reservations/" + id)
                    .then().log().all();
        }
    }
}
