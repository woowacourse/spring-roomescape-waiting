package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.integration.support.RestAssuredTestHelper.createReservation;
import static roomescape.integration.support.RestAssuredTestHelper.createReservationTime;
import static roomescape.integration.support.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.integration.support.DatabaseHelper;
import roomescape.integration.support.SpringWebTest;
import roomescape.time.controller.dto.ReservationTimeResponse;

@SpringWebTest
public class ReservationTimeIntegrationTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대해 예약 가능한 시간 목록을 조회한다.")
    void getAvailableTimes_returnsAvailableTimes() {
        createReservationTime("10:00");
        createReservationTime("11:00");
        createReservationTime("12:00");
        createReservationTime("13:00");

        createTheme("우아한 테마", "우아한테크코스 전용 테마입니다.", "https://example.com/image.png");
        createTheme("페어 테마", "페어 전용 테마입니다.", "https://example.com/pair.png");

        List<ReservationTimeResponse> beforeReservationResults = getAvailableTimes(LocalDate.of(2026, 5, 5), 1L);

        assertThat(beforeReservationResults).hasSize(4);
        assertThat(beforeReservationResults.stream().map(ReservationTimeResponse::id).toList())
                .containsExactly(1L, 2L, 3L, 4L);
        assertThat(beforeReservationResults.stream().map(ReservationTimeResponse::startAt).toList())
                .containsExactly(
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 0),
                        LocalTime.of(13, 0)
                );

        createReservation("브라운", LocalDate.of(2026, 5, 5), 1L, 1L);
        createReservation("포비", LocalDate.of(2026, 5, 6), 2L, 2L);

        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 5), 1L)).hasSize(3);
        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 6), 1L)).hasSize(4);
        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 5), 2L)).hasSize(4);
        assertThat(getAvailableTimes(LocalDate.of(2026, 5, 6), 2L)).hasSize(3);
    }

    private List<ReservationTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {
        return RestAssured.given()
                .queryParam("date", date.toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationTimeResponse.class);
    }
}
