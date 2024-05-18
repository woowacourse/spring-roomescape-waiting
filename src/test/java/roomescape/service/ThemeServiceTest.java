package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.service.exception.ThemeUsedException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Test
    @DisplayName("예약이 있는 테마를 삭제할 경우 예외가 발생한다.")
    void invalidDelete() {
        assertThatThrownBy(() -> themeService.deleteTheme(2L))
                .isInstanceOf(ThemeUsedException.class);
    }

    @Test
    @DisplayName("인기 테마 조회시 from이 until보다 앞일 경우 예외가 발생한다.")
    void invalidPopularDate() {
        final LocalDate now = LocalDate.now();
        assertThatThrownBy(() -> themeService.getPopularThemes(now.minusDays(1), now.minusDays(8), 10))
                .isInstanceOf(InvalidRequestException.class);
    }
}
