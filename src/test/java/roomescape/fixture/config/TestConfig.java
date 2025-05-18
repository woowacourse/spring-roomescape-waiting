package roomescape.fixture.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeService;
import roomescape.theme.infrastructure.ThemeRepository;

@TestConfiguration
public class TestConfig {

    @Bean
    public ReservationTimeService reservationTimeService(
            final ReservationTimeRepository reservationTimeRepository) {
        return new ReservationTimeService(reservationTimeRepository);
    }

    @Bean
    public ThemeService themeService(final ThemeRepository themeRepository) {
        return new ThemeService(themeRepository);
    }

    @Bean
    public ReservationService reservationService(final ReservationRepository reservationRepository,
                                                 final ReservationTimeRepository reservationTimeRepository,
                                                 final ThemeRepository themeRepository,
                                                 final MemberRepository memberRepository) {
        return new ReservationService(reservationRepository, reservationTimeRepository,
                themeRepository, memberRepository);
    }
}
