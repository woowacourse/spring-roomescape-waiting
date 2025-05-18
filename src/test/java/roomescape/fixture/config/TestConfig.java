package roomescape.fixture.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.MemberRepositoryImpl;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationTimeRepository;
import roomescape.reservation.infrastructure.ReservationRepositoryImpl;
import roomescape.reservation.infrastructure.ReservationTimeRepositoryImpl;
import roomescape.theme.application.ThemeService;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.ThemeRepositoryImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    public ReservationTimeRepositoryImpl reservationTimeRepository(
            final JpaReservationTimeRepository jpaReservationTimeRepository) {
        return new ReservationTimeRepositoryImpl(jpaReservationTimeRepository);
    }

    @Bean
    public ThemeRepositoryImpl themeRepository(final JpaThemeRepository jpaThemeRepository) {
        return new ThemeRepositoryImpl(jpaThemeRepository);
    }

    @Bean
    public MemberRepositoryImpl memberRepository(final JpaMemberRepository jpaMemberRepository) {
        return new MemberRepositoryImpl(jpaMemberRepository);
    }

    @Bean
    public ReservationRepositoryImpl reservationRepositoryImpl(
            final JpaReservationRepository jpaReservationRepository) {
        return new ReservationRepositoryImpl(jpaReservationRepository);
    }

    @Bean
    public ReservationTimeService reservationTimeService(
            final ReservationTimeRepositoryImpl reservationTimeRepository) {
        return new ReservationTimeService(reservationTimeRepository, reservationTimeRepository);
    }

    @Bean
    public ThemeService themeService(final ThemeRepositoryImpl themeRepository) {
        return new ThemeService(themeRepository, themeRepository);
    }

    @Bean
    public ReservationService reservationService(final ReservationRepositoryImpl reservationRepository,
                                                 final ReservationTimeRepositoryImpl reservationTimeRepository,
                                                 final ThemeRepositoryImpl themeRepository,
                                                 final MemberRepositoryImpl memberRepository) {
        return new ReservationService(reservationRepository, reservationRepository, reservationTimeRepository,
                themeRepository, memberRepository);
    }
}
