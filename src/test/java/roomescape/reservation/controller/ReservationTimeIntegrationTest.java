package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.testSupport.RestAssuredTestHelper.createReservation;
import static roomescape.testSupport.RestAssuredTestHelper.createReservationTime;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.time.controller.dto.AvailableTimeResponse;

@SpringWebTest
public class ReservationTimeIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

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

        List<AvailableTimeResponse> beforeReservationResults = getAvailableTimes(LocalDate.of(2026, 5, 5), 1L);

        assertThat(beforeReservationResults).hasSize(4);
        assertThat(beforeReservationResults.stream().map(AvailableTimeResponse::id).toList())
                .containsExactly(1L, 2L, 3L, 4L);
        assertThat(beforeReservationResults.stream().map(AvailableTimeResponse::startAt).toList())
                .containsExactly(
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 0),
                        LocalTime.of(13, 0)
                );
        assertThat(beforeReservationResults.stream().filter(AvailableTimeResponse::alreadyBooked).count())
                .isEqualTo(0);

        createReservation("브라운", LocalDate.now().plusDays(1), 1L, 1L);
        createReservation("포비", LocalDate.now().plusDays(2), 2L, 2L);

        List<AvailableTimeResponse> results1 = getAvailableTimes(LocalDate.now().plusDays(1), 1L);
        assertThat(results1).hasSize(4);
        assertThat(results1.stream().filter(AvailableTimeResponse::alreadyBooked).count()).isEqualTo(1);
        assertThat(results1.stream().filter(AvailableTimeResponse::alreadyBooked).findFirst().get().id()).isEqualTo(1L);

        List<AvailableTimeResponse> results2 = getAvailableTimes(LocalDate.now().plusDays(2), 1L);
        assertThat(results2).hasSize(4);
        assertThat(results2.stream().filter(AvailableTimeResponse::alreadyBooked).count()).isEqualTo(0);

        List<AvailableTimeResponse> results3 = getAvailableTimes(LocalDate.now().plusDays(1), 2L);
        assertThat(results3).hasSize(4);
        assertThat(results3.stream().filter(AvailableTimeResponse::alreadyBooked).count()).isEqualTo(0);

        List<AvailableTimeResponse> results4 = getAvailableTimes(LocalDate.now().plusDays(2), 2L);
        assertThat(results4).hasSize(4);
        assertThat(results4.stream().filter(AvailableTimeResponse::alreadyBooked).count()).isEqualTo(1);
        assertThat(results4.stream().filter(AvailableTimeResponse::alreadyBooked).findFirst().get().id()).isEqualTo(2L);
    }

    private List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {
        return RestAssured.given()
                .queryParam("date", date.toString())
                .queryParam("themeId", themeId)
                .when().get("/times/available-times")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", AvailableTimeResponse.class);
    }
}
