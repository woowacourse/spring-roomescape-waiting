package roomescape.acceptance;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.BasicAcceptanceTest;
import roomescape.dto.AvailableTimeResponse;

class ReservationTimeAcceptanceTest extends BasicAcceptanceTest {
    @TestFactory
    @DisplayName("동일한 예약 시간을 두번 추가하면, 예외가 발생한다")
    Stream<DynamicTest> duplicateReservationTest() {
        return Stream.of(
                dynamicTest("예약 시간을 추가한다 (10:00)", () -> postReservationTime("10:00", 201)),
                dynamicTest("동일한 예약 시간을 추가한다 (10:00)", () -> postReservationTime("10:00", 409))
        );
    }

    @TestFactory
    @DisplayName("3개의 예약 시간을 추가한다")
    Stream<DynamicTest> reservationPostTest() {
        return Stream.of(
                dynamicTest("예약 시간을 추가한다 (09:00)", () -> postReservationTime("09:00", 201)),
                dynamicTest("예약 시간을 추가한다 (10:00)", () -> postReservationTime("10:00", 201)),
                dynamicTest("예약 시간을 추가한다 (11:00)", () -> postReservationTime("11:00", 201)),
                dynamicTest("모든 예약 시간을 조회한다 (총 7개)", () -> getReservationTimes(200, 7))
        );
    }

    @TestFactory
    @DisplayName("예약 시간을 추가하고 삭제한다")
    Stream<DynamicTest> reservationPostAndDeleteTest() {
        AtomicLong reservationTimeId = new AtomicLong();

        return Stream.of(
                dynamicTest("예약 시간을 추가한다 (10:00)", () -> reservationTimeId.set(postReservationTime("10:00", 201))),
                dynamicTest("예약 시간을 삭제한다 (10:00)", () -> deleteReservationTime(reservationTimeId.longValue(), 204)),
                dynamicTest("예약 시간을 추가한다 (10:00)", () -> postReservationTime("10:00", 201)),
                dynamicTest("모든 예약 시간을 조회한다 (총 5개)", () -> getReservationTimes(200, 5))
        );
    }

    @TestFactory
    @DisplayName("예약이 가능한 시간을 구분하여 반환한다.")
    Stream<DynamicTest> res() {
        AtomicReference<String> token = new AtomicReference<>();
        return Stream.of(
                dynamicTest("모든 예약된 시간을 조회한다 (총 3개)", () -> getAvailableTimes(200, 3)),
                dynamicTest("로그인을 한다", () -> token.set(LoginUtil.login("member@wooteco.com", "wootecoCrew6!", 200))),
                dynamicTest("예약을 추가한다", () -> ReservationCRD.postClientReservation(token.get(), "2099-04-29", 4L, 1L, 201)),
                dynamicTest("모든 예약된 시간을 조회한다 (총 4개)", () -> getAvailableTimes(200, 4))
        );
    }

    private Long postReservationTime(String time, int expectedHttpCode) {
        Map<?, ?> requestBody = Map.of("startAt", time);

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/times")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        if (expectedHttpCode == 201) {
            return response.jsonPath().getLong("id");
        }

        return null;
    }

    private void getReservationTimes(int expectedHttpCode, int expectedReservationTimesSize) {
        Response response = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<?> reservationTimeResponses = response.as(List.class);

        assertThat(reservationTimeResponses).hasSize(expectedReservationTimesSize);
    }

    private void deleteReservationTime(Long reservationTimeId, int expectedHttpCode) {
        RestAssured.given().log().all()
                .when().delete("/times/" + reservationTimeId)
                .then().log().all()
                .statusCode(expectedHttpCode);
    }

    private void getAvailableTimes(int expectedHttpCode, int expectedAlreadyBookedSize) {
        Response response = RestAssured.given().log().all()
                .when().get("/times/available?date=2099-04-29&themeId=1")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<AvailableTimeResponse> reservationTimeResponses = response.jsonPath().getList(".", AvailableTimeResponse.class);

        List<AvailableTimeResponse> list = reservationTimeResponses.stream()
                .filter(AvailableTimeResponse::isBooked)
                .toList();

        assertThat(list).hasSize(expectedAlreadyBookedSize);
    }
}
