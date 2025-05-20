package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.util.DateTime;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.dto.response.PopularThemeResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ThemeServiceMockTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private DateTime dateTime;

    @InjectMocks
    private ThemeService themeService;

    @DisplayName("존재하는 예약의 테마는 삭제할 수 없다.")
    @Test
    void can_not_remove_exists_reservation() {
        // given
        when(reservationRepository.existsByThemeId(1L))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("인기 테마를 가져올 수 있다.")
    @Test
    void can_get_popular_theme() {
        // given
        when(themeRepository.findPopularThemes(any(ReservationPeriod.class), eq(10)))
                .thenReturn(List.of(
                        Theme.createWithId(1L, "테스트1", "설명", "썸네일"),
                        Theme.createWithId(2L, "테스트3", "설명", "썸네일")
                ));
        when(dateTime.nowDate())
                .thenReturn(LocalDate.now());
        List<PopularThemeResponse> popularThemes = themeService.getPopularThemes();

        // when & then
        assertThat(popularThemes).containsExactly(
                new PopularThemeResponse("테스트1", "썸네일", "설명"),
                new PopularThemeResponse("테스트3", "썸네일", "설명")
        );
    }
}
