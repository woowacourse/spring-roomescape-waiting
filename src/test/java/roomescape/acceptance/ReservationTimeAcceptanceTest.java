package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import roomescape.global.dto.ErrorResponse;
import roomescape.reservation.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;

class ReservationTimeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("[Step7] 예약 시간을 추가한다.")
    void createReservationTime() {
        // given
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(MIA_RESERVATION_TIME);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .extract();
        ReservationTimeResponse reservationTimeResponse = response.as(ReservationTimeResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusCreated(softly, response);
            softly.assertThat(reservationTimeResponse.id()).isNotNull();
            softly.assertThat(reservationTimeResponse.startAt()).isEqualTo(MIA_RESERVATION_TIME);
        });
    }

    @Test
    @DisplayName("[Step7] 잘못된 형식의 예약 시간을 추가한다.")
    void createReservationTime2() {
        // given
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(15, 3));

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusBadRequest(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }

    @Test
    @DisplayName("[Step7] 예약 시간 목록을 조회한다.")
    void findReservationTimes() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .extract();
        List<ReservationTimeResponse> reservationTimeResponse = Arrays.stream(response.as(ReservationTimeResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(reservationTimeResponse).hasSize(0);
        });
    }

    @TestFactory
    @DisplayName("[Step7] 예약 시간을 추가하고 삭제한다.")
    Stream<DynamicTest> createThenDeleteReservationTime() {
        return Stream.of(
                dynamicTest("예약 시간을 하나 생성한다.", this::createReservationTime),
                dynamicTest("예약 시간이 하나 생성된 예약 목록을 조회한다.", this::findReservationTimesWithSizeOne),
                dynamicTest("예약 시간을 하나 삭제한다.", this::deleteOneReservationTime),
                dynamicTest("예약 시간 목록을 조회한다.", this::findReservationTimes)
        );
    }

    void findReservationTimesWithSizeOne() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .extract();
        List<ReservationTimeResponse> reservationTimeResponses = Arrays.stream(response.as(ReservationTimeResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(reservationTimeResponses).hasSize(1)
                    .extracting(ReservationTimeResponse::id)
                    .isNotNull();
        });
    }

    void deleteOneReservationTime() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .extract();

        // then
        checkHttpStatusNoContent(response);
    }
}
