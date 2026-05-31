package roomescape.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.result.PopularThemeResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ThemeServiceTest {

    private final ThemeRepository themeRepository = mock();
    private final ReservationRepository reservationRepository = mock();
    private final ThemeService service = new ThemeService(themeRepository, reservationRepository);

    @Test
    void 전체_테마_조회_테스트() {
        // given
        List<Theme> themes = List.of(
                new Theme(1L, "테스트 테마1", null, null),
                new Theme(2L, "테스트 테마2", null, null));
        when(themeRepository.findAll())
                .thenReturn(themes);

        // when
        List<Theme> result = service.findAll();

        // then
        assertThat(result).isEqualTo(themes);
        verify(themeRepository, times(1)).findAll();
        verifyNoMoreInteractions(themeRepository, reservationRepository);
    }

    @Test
    void 테마_생성_테스트() {
        // given
        Long id = 1L;
        String name = "테스트 테마";
        String description = "테마 설명";
        String thumbnail = "썸네일 주소";
        Theme theme = new Theme(id, name, description, thumbnail);

        when(themeRepository.insert(any(Theme.class)))
                .thenReturn(id);
        when(themeRepository.findBy(id))
                .thenReturn(Optional.of(theme));

        // when
        Theme result = service.create(name, description, thumbnail);

        // then
        ArgumentCaptor<Theme> captor = ArgumentCaptor.forClass(Theme.class);

        assertAll(
                () -> assertThat(result.getId()).isEqualTo(id),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getDescription()).isEqualTo(description),
                () -> assertThat(result.getThumbnail()).isEqualTo(thumbnail));

        verify(themeRepository, times(1)).insert(captor.capture());
        Theme captured = captor.getValue();

        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getDescription()).isEqualTo(description),
                () -> assertThat(captured.getThumbnail()).isEqualTo(thumbnail));

        verify(themeRepository, times(1)).findBy(id);
        verifyNoMoreInteractions(themeRepository, reservationRepository);
    }

    @Test
    void 테마_삭제_테스트() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByThemeId(id))
                .thenReturn(false);

        // when
        service.delete(id);

        // then
        verify(reservationRepository, times(1)).existsByThemeId(id);
        verify(themeRepository, times(1)).delete(id);
        verifyNoMoreInteractions(themeRepository, reservationRepository);
    }

    @Test
    void 예약이_존재하는_테마는_삭제시_예외_발생() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByThemeId(id))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");

        verify(reservationRepository, times(1)).existsByThemeId(id);
        verify(themeRepository, never()).delete(anyLong());
        verifyNoMoreInteractions(themeRepository, reservationRepository);
    }

    @Test
    void 인기_테마_조회_테스트() {
        // given
        List<PopularThemeResult> popularThemes = List.of(
                new PopularThemeResult(1L, "테스트 테마1", "테마 설명1", "썸네일 주소1", 2L),
                new PopularThemeResult(2L, "테스트 테마2", "테마 설명2", "썸네일 주소2", 1L));
        when(themeRepository.findPopular(any(LocalDate.class), any(LocalDate.class), eq(10)))
                .thenReturn(popularThemes);

        // when
        List<PopularThemeResult> result = service.findWeeklyTopTen();

        // then
        assertThat(result).isEqualTo(popularThemes);
        verify(themeRepository, times(1)).findPopular(any(LocalDate.class), any(LocalDate.class), eq(10));
        verifyNoMoreInteractions(themeRepository, reservationRepository);
    }
}
