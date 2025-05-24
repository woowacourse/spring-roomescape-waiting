package roomescape.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.application.exception.ThemeNotFoundException;
import roomescape.reservation.application.exception.UsingThemeException;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.PopularThemeResponse;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.ThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;


    @DisplayName("테마를 생성할 수 있다")
    @Test
    void createTest() {
        // given
        String name = "테마1";
        String description = "설명1";
        String thumbnail = "썸네일1";
        ThemeRequest request = new ThemeRequest(name, description, thumbnail);

        Theme theme = new Theme(1L, name, description, thumbnail);
        when(themeRepository.save(any(Theme.class))).thenReturn(theme);

        // when
        ThemeResponse response = themeService.create(request);

        // then
        assertThat(response.id()).isEqualTo(theme.getId());
    }

    @DisplayName("모든 테마를 조회할 수 있다")
    @Test
    void getAllTest() {
        // given
        List<Theme> themes = List.of(
                new Theme(1L, "테마1", "설명1", "썸네일1"),
                new Theme(2L, "테마2", "설명2", "썸네일2"),
                new Theme(3L, "테마3", "설명3", "썸네일3")
        );
        when(themeRepository.findAll()).thenReturn(themes);

        // when
        List<ThemeResponse> responses = themeService.getAll();

        // then
        Assertions.assertAll(() -> {
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).name()).isEqualTo("테마1");
            assertThat(responses.get(1).name()).isEqualTo("테마2");
            assertThat(responses.get(2).name()).isEqualTo("테마3");
        });
    }


    @DisplayName("테마 삭제 시 존재하지 않는 테마일 경우 예외가 발생한다.")
    @Test
    void deleteByIdTest_notFoundTheme() {
        // given
        Long themeId = 1L;

        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(themeId))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("예약이 존재하는 테마일 경우 테마를 삭제할 수 없다.")
    @Test
    void deleteByIdTest_existsReservation() {
        // given
        Long themeId = 1L;

        Theme theme = new Theme(themeId, "테마1", "설명1", "썸네일1");

        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByTheme(theme)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(themeId))
                .isInstanceOf(UsingThemeException.class);
    }


    @DisplayName("테마를 삭제할 수 있다")
    @Test
    void deleteByIdTest() {
        // given
        Long themeId = 1L;

        Theme theme = new Theme(themeId, "테마1", "설명1", "썸네일1");

        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByTheme(theme)).thenReturn(false);
        // when
        themeService.deleteById(themeId);

        // then
        verify(themeRepository).delete(theme);
    }

    @DisplayName("지난 7일간 가장 많이 예약된 상위 10개의 테마를 조회한다")
    @Test
    void getPopularThemesSuccess() {
        // given
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(7);

        List<Theme> themes = List.of(
                new Theme(1L, "theme1", "description", "thumbnail"),
                new Theme(2L, "theme2", "description", "thumbnail"),
                new Theme(3L, "theme3", "description", "thumbnail")
        );

        when(reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate))
                .thenReturn(themes);

        when(themeRepository.findById(1L)).thenReturn(Optional.of(themes.get(0)));
        when(themeRepository.findById(2L)).thenReturn(Optional.of(themes.get(1)));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(themes.get(2)));

        // when
        List<PopularThemeResponse> responses = themeService.getPopularThemes();

        // then
        Assertions.assertAll(() -> {
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).name()).isEqualTo("theme1");
            assertThat(responses.get(1).name()).isEqualTo("theme2");
            assertThat(responses.get(2).name()).isEqualTo("theme3");
        });
    }
}
