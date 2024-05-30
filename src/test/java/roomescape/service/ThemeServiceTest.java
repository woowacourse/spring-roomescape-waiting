package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.time.DuplicatedTimeException;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.AvailableReservationTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.response.ThemeResponse;

class ThemeServiceTest extends ServiceTest {

    @Autowired
    private ThemeService themeService;

    @Test
    void 모든_테마를_조회할_수_있다() {
        List<ThemeResponse> responses = themeService.findAllTheme();

        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    void 인기_테마를_조회할_수_있다() {
        jdbcTemplate.update("INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES (?, ?, ?, ?)",
                LocalDate.now().minusDays(1), 1, 1, 1);
        List<ThemeResponse> responses = themeService.findAllPopularTheme();

        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    void 테마를_저장할_수_있다() {
        ThemeRequest request = new ThemeRequest("name", "description", "thumbnail");
        ThemeResponse response = themeService.saveTheme(request);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM theme", Integer.class);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void 테마를_삭제할_수_있다() {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", 1);
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", 2);
        jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", 1);

        themeService.deleteTheme(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM theme", Integer.class);

        assertThat(count).isEqualTo(0);
    }
}