package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.application.AdminReservationThemeService;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.support.IntegrationTestSupport;

class AdminReservationThemeServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminReservationThemeService adminReservationThemeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("테마 삭제시 해당 테마 id를 참조하고 있는 예약이 있다면 예외를 발생시킨다")
    @Test
    void delete() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름", "설명", "썸네일");
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(20);
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, 1L, themeId);

        // when & then
        assertThatThrownBy(() -> adminReservationThemeService.delete(themeId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
