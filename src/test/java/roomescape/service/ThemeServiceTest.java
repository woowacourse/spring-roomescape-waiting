package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Theme;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundThemeException;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ThemeRepository;
import roomescape.service.param.CreateThemeParam;
import roomescape.service.result.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ThemeRepository themeRepository;
    @InjectMocks
    private ThemeService themeService;

    @Test
    void 테마를_전체_조회할_수_있다() {
        // given
        List<Theme> themes = List.of(new Theme(1L, "test1", "description1", "thumbnail1"),
                new Theme(2L, "test2", "description2", "thumbnail2"));
        when(themeRepository.findAll()).thenReturn(themes);

        // when
        List<ThemeResult> themeResults = themeService.findAll();

        // then
        assertThat(themeResults).isEqualTo(List.of(new ThemeResult(1L, "test1", "description1", "thumbnail1"),
                new ThemeResult(2L, "test2", "description2", "thumbnail2")));
    }

    @Test
    void 테마를_생성할_수_있다() {
        // given
        CreateThemeParam createThemeParam = new CreateThemeParam("test1", "description1", "thumbnail1");
        Theme savedTheme = new Theme(1L, "test1", "description1", "thumbnail1");
        when(themeRepository.save(any(Theme.class))).thenReturn(savedTheme);

        // when
        Long id = themeService.create(createThemeParam);

        // then
        assertThat(id).isEqualTo(1L);
    }

    @Test
    void id값으로_테마를_찾을_수_있다() {
        // given
        Theme theme = new Theme(1L, "test1", "description1", "thumbnail1");
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

        // when
        ThemeResult themeResult = themeService.findById(1L);

        // then
        assertThat(themeResult).isEqualTo(new ThemeResult(1L, "test1", "description1", "thumbnail1"));
    }

    @Test
    void id값으로_테마를_찾을때_없다면_예외가_발생한다() {
        // given
        when(themeRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> themeService.findById(1L)).isInstanceOf(NotFoundThemeException.class)
                .hasMessageContaining("id에 해당하는 Theme이 없습니다.");
    }

    @Test
    void id값으로_테마를_삭제할떄_예약에서_id가_사용중이라면_예외를_발생시킨다() {
        // given
        when(reservationRepository.existsByThemeId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(1L)).isInstanceOf(DeletionNotAllowedException.class)
                .hasMessageContaining("해당 테마에 예약이 존재합니다.");
    }
}
