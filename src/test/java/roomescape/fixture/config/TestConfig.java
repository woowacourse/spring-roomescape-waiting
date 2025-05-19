package roomescape.fixture.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.member.application.MemberService;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.MemberRepositoryImpl;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationTimeRepository;
import roomescape.reservation.infrastructure.ReservationRepositoryImpl;
import roomescape.reservation.infrastructure.ReservationTimeRepositoryImpl;
import roomescape.theme.application.ThemeService;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.ThemeRepositoryImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    public ReservationTimeRepository reservationTimeRepository(
            final JpaReservationTimeRepository jpaReservationTimeRepository
    ) {
        return new ReservationTimeRepositoryImpl(jpaReservationTimeRepository);
    }

    @Bean
    public ThemeRepositoryImpl themeRepositoryImpl(
            final JpaThemeRepository jpaThemeRepository
    ) {
        return new ThemeRepositoryImpl(jpaThemeRepository);
    }

    @Bean
    public ThemeRepository themeRepository(
            final JpaThemeRepository jpaThemeRepository
    ) {
        return new ThemeRepositoryImpl(jpaThemeRepository);
    }

    @Bean
    public MemberRepository memberRepository(
            final JpaMemberRepository jpaMemberRepository
    ) {
        return new MemberRepositoryImpl(jpaMemberRepository);
    }

    @Bean
    public ReservationRepository reservationRepository(
            final JpaReservationRepository jpaReservationRepository
    ) {
        return new ReservationRepositoryImpl(jpaReservationRepository);
    }

    @Bean
    public ReservationTimeService reservationTimeService(
            final ReservationTimeRepository reservationTimeRepository
    ) {
        return new ReservationTimeService(reservationTimeRepository);
    }

    @Bean
    public ThemeService themeService(
            final ThemeRepository themeRepository
    ) {
        return new ThemeService(themeRepository);
    }

    @Bean
    public ReservationService reservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        return new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );
    }

    @Bean
    public MemberService memberService(
            final MemberRepository memberRepository
    ) {
        return new MemberService(memberRepository);
    }
}
