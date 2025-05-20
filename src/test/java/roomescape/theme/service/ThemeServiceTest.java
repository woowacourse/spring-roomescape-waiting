package roomescape.theme.service;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.common.util.time.DateTime;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdaptor;
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.service.ThemeServiceTest.ThemeConfig;

@DataJpaTest
@Import(ThemeConfig.class)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @DisplayName("존재하는 예약의 테마는 삭제할 수 없다.")
    @Test
    void can_not_remove_exists_reservation() {
        Assertions.assertThatThrownBy(() -> themeService.deleteThemeById(1L))
                .isInstanceOf(ThemeException.class);
    }

    @DisplayName("인기 테마를 가져올 수 있다.")
    @Test
    void can_get_popular_theme() {
        List<PopularThemeResponse> popularThemes = themeService.getPopularThemes();

        Assertions.assertThat(popularThemes).containsExactly(
                new PopularThemeResponse("테마1", "재밌음", "/image/default.jpg"),
                new PopularThemeResponse("테마3", "놀라움", "/image/default.jpg")
        );
    }

    static class ThemeConfig {

        @Bean
        public DateTime dateTime() {
            return () -> LocalDateTime.of(2025, 4, 29, 10, 0);
        }

        @Bean
        public ThemeRepository themeRepository(JpaThemeRepository jpaThemeRepository) {
            return new JpaThemeRepositoryAdaptor(jpaThemeRepository);
        }

        @Bean
        public ReservationRepository reservationRepository(JpaReservationRepository jpaReservationRepository) {
            return new JpaReservationRepositoryAdapter(jpaReservationRepository);
        }

        @Bean
        public ThemeService themeService(DateTime dateTime, ThemeRepository themeRepository, ReservationRepository reservationRepository) {
            return new ThemeService(dateTime, themeRepository, reservationRepository);
        }
    }
}
