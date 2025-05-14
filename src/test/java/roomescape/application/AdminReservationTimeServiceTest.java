package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.application.AdminReservationTimeService;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.support.IntegrationTestSupport;

class AdminReservationTimeServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminReservationTimeService adminReservationTimeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("예약시간 삭제시 해당 예약시간 id를 참조하고 있는 예약이 있다면 예외를 발생시킨다")
    @Test
    void delete() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름", "설명", "썸네일");
        Long timeId = 1L;
        LocalDate date = LocalDate.now().plusDays(20);
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, timeId, 1L);

        // when & then
        assertThatThrownBy(() -> adminReservationTimeService.delete(timeId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
