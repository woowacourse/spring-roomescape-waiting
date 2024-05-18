package roomescape.domain.theme.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ThemeServiceTest extends ServiceTest {

    @Autowired
    private ThemeService themeService;

    @DisplayName("인기 테마를 알 수 있습니다.")
    @Test
    void should_get_theme_ranking() {
        assertThatCode(() -> themeService.getThemeRanking())
                .doesNotThrowAnyException();
    }

    @DisplayName("모든 테마를 불러올 수 있습니다.")
    @Test
    void should_get_all_theme() {
        List<Theme> allTheme = themeService.findAllTheme();

        assertThat(allTheme).hasSize(5);
    }

    @DisplayName("테마를 추가할 수 있습니다.")
    @Test
    void should_add_theme() {
        Theme savedTheme = themeService.addTheme(new ThemeAddRequest("테마6", "테마6입니당 ^0^", "url6"));

        Theme expectedTheme = new Theme(6L, "테마6", "테마6입니당 ^0^", "url6");

        assertThat(savedTheme).isEqualTo(expectedTheme);
    }

    @DisplayName("존재하지 않는 테마 삭제 요청시 예외가 발생합니다")
    @Test
    void should_throw_EntityNotFoundException_when_theme_id_no_exist() {
        assertThatThrownBy(() -> themeService.removeTheme(6L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 id를 가진 테마가 존재하지 않습니다.");
    }
}
