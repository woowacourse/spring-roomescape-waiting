package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.DuplicateThemeException;
import roomescape.theme.exception.ThemeInUseException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeCommand;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    ThemeRepository themeRepository;

    @DisplayName("테마 생성 시, 기존에 이미 동일한 테마가 있으면 예외가 발생한다.")
    @Test
    void registerTheme_duplicate() {
        //given
        ThemeService themeService = new ThemeService(themeRepository);

        when(themeRepository.existByName("브라운"))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> themeService.registerTheme(
            new ThemeCommand("브라운", "설명", "url")
        )).isInstanceOf(DuplicateThemeException.class);
    }

    @DisplayName("id에 해당하는 테마가 없으면 예외가 발생한다.")
    @Test
    void removeThemeById_not_found() {
        //given
        ThemeService themeService = new ThemeService(themeRepository);

        when(themeRepository.findById(1L))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> themeService.removeThemeById(1L))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("테마 삭제시, 테마가 사용 중이면 예외가 발생한다.")
    @Test
    void removeThemeById_in_use() {
        //given
        ThemeService themeService = new ThemeService(themeRepository);

        when(themeRepository.findById(1L))
                .thenReturn(Optional.of(new Theme(1L, "테마", "설명", "url")));

        when(themeRepository.deleteById(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key"));

        //when & then
        assertThatThrownBy(() -> themeService.removeThemeById(1L))
                .isInstanceOf(ThemeInUseException.class);
    }
}
