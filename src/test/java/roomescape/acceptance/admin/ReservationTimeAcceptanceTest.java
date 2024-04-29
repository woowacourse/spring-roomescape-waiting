package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.controller.exception.CustomExceptionResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_TIME_1;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_TIME_2;

class ReservationTimeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 예약 시간 목록을 조회한다.")
    @Test
    void getReservationTimes_success() {
        TypeRef<List<ReservationTimeResponse>> reservationTimesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(reservationTimesFormat);
    }

    @DisplayName("관리자가 예약 시간을 추가한다.")
    @Test
    void addReservationTime_success() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.parse("12:00:00"));

        RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("location", containsString("/times/"))
                .extract().as(ReservationTimeResponse.class);
    }

    @DisplayName("관리자가 예약 시간을 삭제한다.")
    @Nested
    class deleteReservationTime extends NestedAcceptanceTest {

        @DisplayName("정상 작동")
        @Test
        void deleteReservationTime_forExist_success() {
            long existReservationTimeId = PRE_INSERTED_RESERVATION_TIME_1.getId();

            sendDeleteRequest(existReservationTimeId)
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("예외 발생 - 존재하지 않는 예약 시간을 삭제한다.")
        @Test
        void deleteReservationTime_forNonExist_fail() {
            long notExistTimeId = 0L;

            CustomExceptionResponse response = sendDeleteRequest(notExistTimeId)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().as(CustomExceptionResponse.class);

            assertAll(
                    () -> assertThat(response.title()).contains("리소스를 찾을 수 없습니다."),
                    () -> assertThat(response.detail()).contains("아이디에 해당하는 예약 시간을 찾을 수 없습니다.")
            );
        }

        @DisplayName("예외 발생 - 예약이 있는 예약 시간을 삭제한다.")
        @Test
        void deleteReservationTime_whenReservationExist_fail() {
            long timeIdWhereReservationExist = PRE_INSERTED_RESERVATION_TIME_2.getId();

            CustomExceptionResponse response = sendDeleteRequest(timeIdWhereReservationExist)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(CustomExceptionResponse.class);

            assertAll(
                    () -> assertThat(response.title()).contains("허용되지 않는 작업입니다."),
                    () -> assertThat(response.detail()).contains("해당 시간에 예약이 존재하기 때문에 삭제할 수 없습니다.")
            );
        }

        private ValidatableResponse sendDeleteRequest(long existReservationTimeId) {
            return RestAssured.given().log().all()
                    .when().delete("/times/" + existReservationTimeId)
                    .then().log().all();
        }
    }
}
