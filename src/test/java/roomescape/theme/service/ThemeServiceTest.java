package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import roomescape.global.exception.DuplicateException;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.BadRequestException;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.theme.domain.Theme;



import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeCommand;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    ThemeRepository themeRepository;

    @Test
    @DisplayName("테마 생성 시, 기존에 이미 동일한 테마가 있으면 예외가 발생한다.")
    void registerTheme_duplicate() {
        //given
        ThemeService themeService = new ThemeService(themeRepository);

        given(themeRepository.existsByName("브라운"))
                .willReturn(true);

        //when & then
        assertThatThrownBy(() -> themeService.save(
            new ThemeCommand("브라운", "설명", "url")
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ThemeErrorCode.DUPLICATE_THEME.getMessage());
    }

    @Test
    @DisplayName("id에 해당하는 테마가 없으면 예외가 발생한다.")
    void removeThemeById_not_found() {
        //given
        ThemeService themeService = new ThemeService(themeRepository);

        given(themeRepository.deleteById(1L))
                .willReturn(0);

        //when & then
        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("테마 삭제시, 테마가 사용 중이면 예외가 발생한다.")
    void removeThemeById_in_use() {
        //given
        ThemeService themeService = new ThemeService(themeRepository);

        given(themeRepository.deleteById(1L))
                .willThrow(new DataIntegrityViolationException("foreign key"));

        //when & then
        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ThemeErrorCode.THEME_IN_USE.getMessage());
    }
}
