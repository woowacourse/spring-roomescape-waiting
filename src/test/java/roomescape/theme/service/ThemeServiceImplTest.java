package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.holiday.repository.HolidayRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeSaveServiceDto;
import roomescape.time.service.TimeService;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private TimeService timeService;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private ThemeServiceImpl themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeServiceImpl(themeRepository, timeService, holidayRepository, reservationRepository, 7, 10);
    }

    @DisplayName("테마 목록을 조회한다.")
    @Test
    void getAll_테마_목록_조회() {
        List<Theme> themes = List.of(
                new Theme("a", "b", "c").withId(1L),
                new Theme("d", "e", "f").withId(2L));
        when(themeRepository.findAll()).thenReturn(themes);

        assertThat(themeService.getAll()).isEqualTo(themes);
        verify(themeRepository).findAll();
    }

    @DisplayName("테마를 생성한다.")
    @Test
    void create_테마_생성() {
        ThemeSaveServiceDto dto = new ThemeSaveServiceDto("이름", "설명", "https://url");
        Theme persisted = new Theme("이름", "설명", "https://url").withId(10L);
        when(themeRepository.save(any(Theme.class))).thenReturn(persisted);

        Theme result = themeService.create(dto);

        assertThat(result).isEqualTo(persisted);

        ArgumentCaptor<Theme> captor = ArgumentCaptor.forClass(Theme.class);
        verify(themeRepository).save(captor.capture());
        Theme passed = captor.getValue();
        assertThat(passed.getId()).isNull();
        assertThat(passed.getName()).isEqualTo("이름");
        assertThat(passed.getDescription()).isEqualTo("설명");
        assertThat(passed.getImageUrl()).isEqualTo("https://url");
    }

    @DisplayName("id로 테마를 삭제한다.")
    @Test
    void deleteById() {
        when(themeRepository.deleteById(1L)).thenReturn(true);

        themeService.deleteById(1L);

        verify(themeRepository).deleteById(1L);
    }

    @DisplayName("존재하지 않는 테마 삭제 시, 예외가 발생한다.")
    @Test
    void deleteById_없으면_예외() {
        when(themeRepository.deleteById(99L)).thenReturn(false);

        assertThatThrownBy(() -> themeService.deleteById(99L))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("테마를 찾을 수 없습니다. id=99");
    }

    @DisplayName("id에 해당하는 테마가 존재하지 않는 경우, 예외가 발생한다.")
    @Test
    void getAvailableTimes_테마가없으면_예외() {
        LocalDate date = LocalDate.of(2026, 5, 6);
        when(themeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> themeService.getAvailableTimes(999L, date))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("테마를 찾을 수 없습니다. id=999");

        verifyNoInteractions(timeService, holidayRepository, reservationRepository);
    }

    @DisplayName("날짜가 휴일인 경우, 빈 리스트를 반환한다.")
    @Test
    void getAvailableTimes_휴일이면_빈_리스트() {
        LocalDate date = LocalDate.of(2026, 5, 6);
        when(themeRepository.existsById(1L)).thenReturn(true);
        when(holidayRepository.existsByDate(date)).thenReturn(true);

        assertThat(themeService.getAvailableTimes(1L, date)).isEmpty();

        verify(holidayRepository).existsByDate(date);
        verifyNoInteractions(timeService, reservationRepository);
    }

    @DisplayName("리포지토리 결과가 없는 경우, 빈 리스트를 그대로 반환한다.")
    @Test
    void getBestThemes_빈_결과() {
        // given
        when(themeRepository.findBestThemesByDate(any(LocalDate.class), any(LocalDate.class), eq(10)))
                .thenReturn(Collections.emptyList());

        // when
        List<Theme> result = themeService.getBestThemes();

        // then
        assertThat(result).isEmpty();
    }
}
