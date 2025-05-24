package roomescape.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.PopularThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    @Nested
    @DisplayName("인기 테마 목록 조회")
    class GetPopularThemesTest {

        @Test
        @DisplayName("지난 7일간 가장 많이 예약된 상위 10개의 테마를 조회한다")
        void getPopularThemesSuccess() {
            // given
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(7);

            List<Theme> themes = List.of(
                    new Theme(1L, "theme1", "description", "thumbnail"),
                    new Theme(2L, "theme2", "description", "thumbnail"),
                    new Theme(3L, "theme3", "description", "thumbnail")
            );

            given(reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate))
                    .willReturn(themes);

            given(themeRepository.findById(1L)).willReturn(Optional.of(themes.get(0)));
            given(themeRepository.findById(2L)).willReturn(Optional.of(themes.get(1)));
            given(themeRepository.findById(3L)).willReturn(Optional.of(themes.get(2)));

            // when
            List<PopularThemeResponse> responses = themeService.getPopularThemes();

            // then
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).name()).isEqualTo("theme1");
            assertThat(responses.get(1).name()).isEqualTo("theme2");
            assertThat(responses.get(2).name()).isEqualTo("theme3");
        }
    }
}
