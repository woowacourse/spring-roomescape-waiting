package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.result.PopularThemeResult;

class ThemeServiceTest {

    private final JpaThemeRepository themeRepository = mock();
    private final ThemeRepository themeQueryRepository = mock();
    private final ReservationRepository reservationRepository = mock();
    private final ReservationWaitingRepository reservationWaitingRepository = mock();
    private final ThemeService service = new ThemeService(
            themeRepository,
            themeQueryRepository,
            reservationRepository,
            reservationWaitingRepository);

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
    }

    @Test
    void 테마_생성_테스트() {
        // given
        Long id = 1L;
        String name = "테스트 테마";
        String description = "테마 설명";
        String thumbnail = "썸네일 주소";
        Theme theme = new Theme(id, name, description, thumbnail);

        when(themeRepository.save(any(Theme.class)))
                .thenReturn(theme);
        when(themeRepository.findById(id))
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

        verify(themeRepository, times(1)).save(captor.capture());
        Theme captured = captor.getValue();

        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getDescription()).isEqualTo(description),
                () -> assertThat(captured.getThumbnail()).isEqualTo(thumbnail));
    }

    @Test
    void 테마_삭제_테스트() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByThemeId(id))
                .thenReturn(false);
        when(reservationWaitingRepository.existsByThemeId(id))
                .thenReturn(false);

        // when
        service.delete(id);

        // then
        verify(themeRepository).deleteById(id);
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

        verify(themeRepository, never()).deleteById(anyLong());
    }

    @Test
    void 예약_대기가_존재하는_테마는_삭제시_예외_발생() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByThemeId(id))
                .thenReturn(false);
        when(reservationWaitingRepository.existsByThemeId(id))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약 대기가 존재하는 테마는 삭제할 수 없습니다.");

        verify(themeRepository, never()).deleteById(anyLong());
    }

    @Test
    void 인기_테마_조회_테스트() {
        // given
        List<PopularThemeResult> popularThemes = List.of(
                new PopularThemeResult(1L, "테스트 테마1", "테마 설명1", "썸네일 주소1", 2L),
                new PopularThemeResult(2L, "테스트 테마2", "테마 설명2", "썸네일 주소2", 1L));
        when(themeQueryRepository.findPopular(any(LocalDate.class), any(LocalDate.class), eq(10)))
                .thenReturn(popularThemes);

        // when
        List<PopularThemeResult> result = service.findWeeklyTopTen();

        // then
        assertThat(result).isEqualTo(popularThemes);
    }
}
