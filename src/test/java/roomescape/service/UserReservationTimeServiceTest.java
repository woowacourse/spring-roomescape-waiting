package roomescape.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.application.UserReservationTimeService;
import roomescape.domain.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.support.IntegrationTestSupport;

class UserReservationTimeServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserReservationTimeService userReservationTimeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("입력된 테마와 날짜에 대해, 전체 예약 시간과 각각의 예약 상태를 함께 조회할 수 있다")
    @Test
    void getAllWithStatus() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:05");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:10");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "테마이름", "설명", "썸네일");
        Long themeId = 1L;
        Long firstTimeId = 1L;
        Long secondTimeId = 2L;
        LocalDate date = LocalDate.now().plusDays(20);
        String name = "웨이드";

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", name, date,
                firstTimeId, themeId);
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", name, date,
                secondTimeId, themeId);
        // when
        List<ReservationTimeServiceResponse> responses = userReservationTimeService.getAllWithStatus(themeId, date);

        // then
        List<ReservationTimeServiceResponse> bookedResponse = responses.stream()
                .filter(ReservationTimeServiceResponse::isBooked)
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(3);
            softly.assertThat(bookedResponse).hasSize(2);
            softly.assertThat(bookedResponse.getFirst().id()).isEqualTo(firstTimeId);
            softly.assertThat(bookedResponse.get(1).id()).isEqualTo(secondTimeId);
        });
    }
}
