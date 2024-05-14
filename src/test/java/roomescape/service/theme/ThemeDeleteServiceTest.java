package roomescape.service.theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
class ThemeDeleteServiceTest {

    private ThemeDeleteService themeDeleteService;

    @Autowired
    public ThemeDeleteServiceTest(JdbcTemplate jdbcTemplate) {
        themeDeleteService = new ThemeDeleteService(
                new ThemeRepository(jdbcTemplate),
                new ReservationRepository(jdbcTemplate)
        );
    }

    @Test
    @DisplayName("예약 중이 아닌 테마를 삭제할 시 성공한다.")
    void deleteNotReservedTime_Success() {
        assertThatCode(() -> themeDeleteService.deleteTheme(2L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약 중인 테마를 삭제할 시 예외가 발생한다.")
    void deleteReservedTime_Failure() {
        assertThatThrownBy(() -> themeDeleteService.deleteTheme(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약중인 테마는 삭제할 수 없습니다.");
    }
}
