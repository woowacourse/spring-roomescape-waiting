package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.controller.theme.dto.CreateThemeRequest;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.service.exception.ThemeUsedException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ThemeServiceTest extends IntegrationTestSupport {

    @Autowired
    ThemeService themeService;

    @Test
    @DisplayName("테마를 저장한다.")
    void saveTheme() {
        final CreateThemeRequest request = new CreateThemeRequest("name", "설명", "섬네일");
        final ThemeResponse theme = themeService.addTheme(request);
        final ThemeResponse expected = new ThemeResponse(theme.id(), "name", "설명", "섬네일");

        assertThat(theme).isEqualTo(expected);
    }

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
        final LocalDate from = now.minusDays(1);
        final LocalDate until = now.minusDays(8);
        assertThatThrownBy(() -> themeService.getPopularThemes(from, until, 10))
                .isInstanceOf(InvalidRequestException.class);
    }
}
