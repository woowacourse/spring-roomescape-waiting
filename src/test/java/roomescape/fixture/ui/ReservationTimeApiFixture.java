package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import roomescape.reservation.ui.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.ui.dto.response.ReservationTimeResponse;

public class ReservationTimeApiFixture {

    public static final List<CreateReservationTimeRequest> RESERVATIONS_TIME_REQUESTS = List.of(
            new CreateReservationTimeRequest(LocalTime.of(10, 0)),
            new CreateReservationTimeRequest(LocalTime.of(11, 0)),
            new CreateReservationTimeRequest(LocalTime.of(12, 0)),
            new CreateReservationTimeRequest(LocalTime.of(13, 0)),
            new CreateReservationTimeRequest(LocalTime.of(14, 0)),
            new CreateReservationTimeRequest(LocalTime.of(15, 0)),
            new CreateReservationTimeRequest(LocalTime.of(16, 0)),
            new CreateReservationTimeRequest(LocalTime.of(17, 0)),
            new CreateReservationTimeRequest(LocalTime.of(18, 0)),
            new CreateReservationTimeRequest(LocalTime.of(19, 0)),
            new CreateReservationTimeRequest(LocalTime.of(20, 0)),
            new CreateReservationTimeRequest(LocalTime.of(21, 0)),
            new CreateReservationTimeRequest(LocalTime.of(22, 0))
    );

    private ReservationTimeApiFixture() {
    }

    public static CreateReservationTimeRequest reservationTimeRequest1() {
        if (RESERVATIONS_TIME_REQUESTS.isEmpty()) {
            throw new IllegalStateException("예약 픽스처의 개수가 부족합니다.");
        }
        return RESERVATIONS_TIME_REQUESTS.get(0);
    }

    public static CreateReservationTimeRequest reservationTimeRequest2() {
        if (RESERVATIONS_TIME_REQUESTS.size() < 2) {
            throw new IllegalStateException("예약 픽스처의 개수가 부족합니다.");
        }
        return RESERVATIONS_TIME_REQUESTS.get(1);
    }

    public static List<ReservationTimeResponse> createReservationTimes(
            final Map<String, String> cookies,
            final int count
    ) {
        if (count > RESERVATIONS_TIME_REQUESTS.size()) {
            throw new IllegalStateException("예약 픽스처의 개수는 최대 " + RESERVATIONS_TIME_REQUESTS.size() + "개만 가능합니다.");
        }

        return RESERVATIONS_TIME_REQUESTS.stream()
                .limit(count)
                .map(reservationTimeParams -> RestAssured.given().log().all()
                        .cookies(cookies)
                        .contentType(ContentType.JSON)
                        .body(reservationTimeParams)
                        .when().post("/times")
                        .then().log().all()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract().as(ReservationTimeResponse.class))
                .toList();
    }
}
