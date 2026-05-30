package roomescape.feature.reservation.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.repository.TimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationAvailabilityFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void 예약_가능_시간_조회_후_예약을_생성하면_같은_테마에서만_예약된_시간의_available이_false가_된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "http://image1.png"));

        // when
        List<TimeAvailabilityResponseDto> beforeReservationTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", theme.getId())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.getId(), theme.getId()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201)
            .body("name", equalTo("예약자"))
            .body("date", equalTo(date.toString()))
            .body("timeId", equalTo(time.getId().intValue()))
            .body("themeId", equalTo(theme.getId().intValue()));

        List<TimeAvailabilityResponseDto> afterReservationTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", theme.getId())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        boolean beforeAllAvailable = beforeReservationTimes.stream().allMatch(TimeAvailabilityResponseDto::available);
        assertThat(beforeAllAvailable).isTrue();

        boolean afterNotAvailable = afterReservationTimes.stream()
            .filter(timeAvailability -> Objects.equals(timeAvailability.id(), time.getId()))
            .noneMatch(TimeAvailabilityResponseDto::available);

        boolean afterTimeContains = afterReservationTimes.stream()
            .anyMatch(timeAvailability -> Objects.equals(timeAvailability.id(), time.getId()));

        assertThat(afterNotAvailable).isTrue();
        assertThat(afterTimeContains).isTrue();
    }
}
