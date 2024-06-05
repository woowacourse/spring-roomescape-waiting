package roomescape.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import static roomescape.acceptance.Fixture.PRE_INSERTED_RESERVATION_TIME_1;
import static roomescape.acceptance.Fixture.PRE_INSERTED_RESERVATION_TIME_2;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.exception.RoomescapeExceptionCode.CANNOT_DELETE_TIME_REFERENCED_BY_RESERVATION;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_NOT_FOUND;
import static roomescape.util.CookieUtil.TOKEN_NAME;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.acceptance.NestedAcceptanceTest;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.ExceptionResponse;

class ReservationTimeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("관리자가 예약 시간 목록을 조회한다.")
    @Test
    void getReservationTimes_success() {
        TypeRef<List<ReservationTimeResponse>> reservationTimesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .cookie(TOKEN_NAME, adminToken)
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
                .cookie(TOKEN_NAME, adminToken)
                .body(reservationTimeRequest)
                .when().post("/admin/times")
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

            ExceptionResponse response = sendDeleteRequest(notExistTimeId)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().as(ExceptionResponse.class);

            assertThat(response.message()).contains(RESERVATION_NOT_FOUND.message());
        }

        @DisplayName("예외 발생 - 예약이 있는 예약 시간을 삭제한다.")
        @Test
        void deleteReservationTime_whenReservationExist_fail() {
            long timeIdWhereReservationExist = PRE_INSERTED_RESERVATION_TIME_2.getId();

            ExceptionResponse response = sendDeleteRequest(timeIdWhereReservationExist)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(ExceptionResponse.class);

            assertThat(response.message()).contains(CANNOT_DELETE_TIME_REFERENCED_BY_RESERVATION.message());
        }

        private ValidatableResponse sendDeleteRequest(long existReservationTimeId) {
            return RestAssured.given().log().all()
                    .cookie(TOKEN_NAME, adminToken)
                    .when().delete("/admin/times/" + existReservationTimeId)
                    .then().log().all();
        }
    }
}
