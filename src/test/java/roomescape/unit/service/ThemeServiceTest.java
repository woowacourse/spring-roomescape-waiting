package roomescape.unit.service;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dto.request.CreateThemeRequest;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ThemeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThemeServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 테마를_추가할_수_있다() {
        // given
        CreateThemeRequest request = new CreateThemeRequest("방탈출", "게임입니다.", "thumbnail");
        when(themeRepository.existsByName(any(String.class)))
                .thenReturn(false);
        when(themeRepository.save(any(Theme.class)))
                .thenReturn(new Theme(1L, request.name(), request.description(), request.thumbnail()));

        // when
        Theme addedTheme = themeService.addTheme(request);

        //then
        assertThat(addedTheme.getId()).isEqualTo(1L);
    }

    @Test
    void 테마를_조회할_수_있다() {
        // given
        Theme theme = new Theme("방탈출", "게임입니다.", "thumbnail");
        Theme theme2 = new Theme("방탈출2", "게임입니다.", "thumbnail");

        when(themeRepository.findAll()).thenReturn(List.of(theme,theme2));

        // when & then
        assertThat(themeService.findAll()).hasSize(2);
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        themeService.deleteThemeById(1L);

        // when & then
        verify(themeRepository, times(1)).deleteById(1L);
    }

    @Test
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        // given
        when(reservationRepository.existsByThemeId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(1L))
                .isInstanceOf(InvalidThemeException.class);
    }
}
