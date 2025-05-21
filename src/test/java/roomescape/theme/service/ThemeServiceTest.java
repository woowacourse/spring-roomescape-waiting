package roomescape.theme.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdaptor;
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.service.ThemeServiceTest.ThemeConfig;

@DataJpaTest
@Import(ThemeConfig.class)
class ThemeServiceTest {

    @Autowired
    private ThemeFacadeService themeService;

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @DisplayName("존재하는 예약의 테마는 삭제할 수 없다.")
    @Test
    void can_not_remove_exists_reservation() {
        Assertions.assertThatThrownBy(() -> themeService.deleteThemeById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("인기 테마를 가져올 수 있다.")
    @Test
    void can_get_popular_theme() {
        // given
        Theme theme1 = jpaThemeRepository.save(new Theme("테마1", "재밌음", "/image/default.jpg"));
        Theme theme2 = jpaThemeRepository.save(new Theme("테마2", "무서움", "/image/default.jpg"));
        Theme theme3 = jpaThemeRepository.save(new Theme("테마3", "놀라움", "/image/default.jpg"));

        ReservationTime time1 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        ReservationTime time2 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        Member member = jpaMemberRepository.save(
            new Member(new Name("율무"), new Email("test@email.com"), new Password("password"))
        );

        LocalDate now = LocalDate.now();
        jpaReservationRepository.save(
            new Reservation(now.minusDays(2), time1, theme1, member, ReservationStatus.RESERVED)
        );
        jpaReservationRepository.save(
            new Reservation(now.minusDays(2), time2, theme1, member, ReservationStatus.RESERVED)
        );
        jpaReservationRepository.save(
            new Reservation(now.minusDays(2), time1, theme3, member, ReservationStatus.RESERVED)
        );

        // when
        List<PopularThemeResponse> popularThemes = themeService.getPopularThemes();

        // then
        Assertions.assertThat(popularThemes).containsExactly(
                new PopularThemeResponse(theme1.getName(), theme1.getDescription(), theme1.getThumbnail()),
                new PopularThemeResponse(theme3.getName(), theme3.getDescription(), theme3.getThumbnail())
        );
    }

    static class ThemeConfig {

        @Bean
        public ThemeRepository themeRepository(JpaThemeRepository jpaThemeRepository) {
            return new JpaThemeRepositoryAdaptor(jpaThemeRepository);
        }

        @Bean
        public ReservationRepository reservationRepository(JpaReservationRepository jpaReservationRepository) {
            return new JpaReservationRepositoryAdapter(jpaReservationRepository);
        }

        @Bean
        public ThemeQueryService themeQueryService(ThemeRepository themeRepository) {
            return new ThemeQueryService(themeRepository);
        }

        @Bean
        public ThemeCommandService themeCommandService(ThemeRepository themeRepository,
                                                       ReservationRepository reservationRepository) {
            return new ThemeCommandService(themeRepository, reservationRepository);
        }

        @Bean
        public ThemeFacadeService themeService(ThemeQueryService themeQueryService,
                                               ThemeCommandService themeCommandService) {
            return new ThemeFacadeService(themeQueryService, themeCommandService);
        }
    }
}
