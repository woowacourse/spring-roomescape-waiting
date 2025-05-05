package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ReservationTimeApiFixture {
    
    public static final List<Map<String, String>> RESERVATIONS_TIME_PARAMS_LIST = List.of(
            Map.of("startAt", "10:00"),
            Map.of("startAt", "11:00"),
            Map.of("startAt", "12:00"),
            Map.of("startAt", "13:00"),
            Map.of("startAt", "14:00"),
            Map.of("startAt", "15:00"),
            Map.of("startAt", "16:00"),
            Map.of("startAt", "17:00"),
            Map.of("startAt", "18:00"),
            Map.of("startAt", "19:00"),
            Map.of("startAt", "20:00"),
            Map.of("startAt", "21:00"),
            Map.of("startAt", "22:00")
    );

    private ReservationTimeApiFixture() {
    }

    public static Map<String, String> reservationTimeParams1() {
        if (RESERVATIONS_TIME_PARAMS_LIST.isEmpty()) {
            throw new IllegalStateException("예약 픽스처의 개수가 부족합니다.");
        }
        return RESERVATIONS_TIME_PARAMS_LIST.get(0);
    }

    public static Map<String, String> reservationTimeParams2() {
        if (RESERVATIONS_TIME_PARAMS_LIST.size() < 2) {
            throw new IllegalStateException("예약 픽스처의 개수가 부족합니다.");
        }
        return RESERVATIONS_TIME_PARAMS_LIST.get(1);
    }

    public static List<ValidatableResponse> createReservationTimes(
            final Map<String, String> cookies,
            final int count
    ) {
        if (count > RESERVATIONS_TIME_PARAMS_LIST.size()) {
            throw new IllegalStateException("예약 픽스처의 개수는 최대 " + RESERVATIONS_TIME_PARAMS_LIST.size() + "개 입니다.");
        }

        return RESERVATIONS_TIME_PARAMS_LIST.stream()
                .limit(count)
                .map(reservationTimeParams -> {
                    return RestAssured.given().log().all()
                            .cookies(cookies)
                            .contentType(ContentType.JSON)
                            .body(reservationTimeParams)
                            .when().post("/times")
                            .then().log().all()
                            .statusCode(HttpStatus.CREATED.value());
                })
                .toList();
    }
}
