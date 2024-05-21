package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.theme.ReservationReferencedThemeException;
import roomescape.service.reservation.ReservationService;
import roomescape.service.theme.ThemeService;
import roomescape.service.theme.dto.ThemeListResponse;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

class ThemeServiceTest extends ServiceTest {
    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationService reservationService;

    @Nested
    @DisplayName("테마 목록 조회")
    class FindAllTheme {
        @Test
        void 테마_목록을_조회할_수_있다() {
            ThemeListResponse response = themeService.findAllTheme();

            assertThat(response.getThemes().size())
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("인기 테마 목록 조회")
    class FindAllPopularTheme {
        @Test
        void 최근_일주일동안_예약_건수_많은_순서대로_10개_테마를_인기_테마로_조회할_수_있다() {
            ThemeListResponse response = themeService.findAllPopularTheme();

            assertThat(response.getThemes().get(0).getId())
                    .isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("테마 추가")
    class SaveTheme {
        @Test
        void 테마를_추가할_수_있다() {
            ThemeRequest request = new ThemeRequest("레벨3", "내용이다.", "https://naver.com");

            ThemeResponse response = themeService.saveTheme(request);

            assertThat(response.getId())
                    .isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("테마 삭제")
    class DeleteTheme {
        @Test
        void 테마를_삭제할_수_있다() {
            reservationService.deleteReservation(1L);

            themeService.deleteTheme(1L);

            ThemeListResponse response = themeService.findAllTheme();
            assertThat(response.getThemes().size())
                    .isEqualTo(0);
        }

        @Test
        void 존재하지_않는_테마_삭제_시_예외가_발생한다() {
            assertThatThrownBy(() -> themeService.deleteTheme(13L))
                    .isInstanceOf(NotFoundThemeException.class);
        }

        @Test
        void 예약이_존재하는_테마_삭제_시_예외가_발생한() {
            assertThatThrownBy(() -> themeService.deleteTheme(1L))
                    .isInstanceOf(ReservationReferencedThemeException.class);
        }
    }
}
