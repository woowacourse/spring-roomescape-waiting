package roomescape.fixture.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.member.application.MemberService;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.member.domain.MemberQueryRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.MemberRepositoryImpl;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationQueryRepository;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.domain.ReservationTimeQueryRepository;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationTimeRepository;
import roomescape.reservation.infrastructure.ReservationRepositoryImpl;
import roomescape.reservation.infrastructure.ReservationTimeRepositoryImpl;
import roomescape.theme.applcation.ThemeService;
import roomescape.theme.domain.ThemeCommandRepository;
import roomescape.theme.domain.ThemeQueryRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.ThemeRepositoryImpl;

@TestConfiguration
public class TestConfig {

    @Bean
    public ReservationTimeQueryRepository reservationTimeQueryRepository(
            final JpaReservationTimeRepository jpaReservationTimeRepository
    ) {
        return new ReservationTimeRepositoryImpl(jpaReservationTimeRepository);
    }

    @Bean
    public ReservationTimeCommandRepository reservationTimeCommandRepository(
            final JpaReservationTimeRepository jpaReservationTimeRepository
    ) {
        return new ReservationTimeRepositoryImpl(jpaReservationTimeRepository);
    }

    @Bean
    public ThemeCommandRepository themeCommandRepository(
            final JpaThemeRepository jpaThemeRepository
    ) {
        return new ThemeRepositoryImpl(jpaThemeRepository);
    }

    @Bean
    public ThemeQueryRepository themeQueryRepository(
            final JpaThemeRepository jpaThemeRepository
    ) {
        return new ThemeRepositoryImpl(jpaThemeRepository);
    }

    @Bean
    public MemberCommandRepository memberCommandRepository(
            final JpaMemberRepository jpaMemberRepository
    ) {
        return new MemberRepositoryImpl(jpaMemberRepository);
    }

    @Bean
    public MemberQueryRepository memberQueryRepository(
            final JpaMemberRepository jpaMemberRepository
    ) {
        return new MemberRepositoryImpl(jpaMemberRepository);
    }

    @Bean
    public ReservationCommandRepository reservationCommandRepository(
            final JpaReservationRepository jpaReservationRepository
    ) {
        return new ReservationRepositoryImpl(jpaReservationRepository);
    }

    @Bean
    public ReservationQueryRepository reservationQueryRepository(
            final JpaReservationRepository jpaReservationRepository
    ) {
        return new ReservationRepositoryImpl(jpaReservationRepository);
    }

    @Bean
    public ReservationTimeService reservationTimeService(
            final ReservationTimeCommandRepository reservationTimeCommandRepository,
            final ReservationTimeQueryRepository reservationTimeQueryRepository
    ) {
        return new ReservationTimeService(reservationTimeCommandRepository, reservationTimeQueryRepository);
    }

    @Bean
    public ThemeService themeService(
            final ThemeCommandRepository themeCommandRepository,
            final ThemeQueryRepository themeQueryRepository
    ) {
        return new ThemeService(themeCommandRepository, themeQueryRepository);
    }

    @Bean
    public ReservationService reservationService(
            final ReservationCommandRepository reservationCommandRepository,
            final ReservationQueryRepository reservationQueryRepository,
            final ReservationTimeQueryRepository reservationTimeQueryRepository,
            final ThemeQueryRepository themeQueryRepository,
            final MemberQueryRepository memberQueryRepository
    ) {
        return new ReservationService(
                reservationCommandRepository,
                reservationQueryRepository,
                reservationTimeQueryRepository,
                themeQueryRepository,
                memberQueryRepository
        );
    }

    @Bean
    public MemberService memberService(
            final MemberCommandRepository memberCommandRepository,
            final MemberQueryRepository memberQueryRepository
    ) {
        return new MemberService(memberCommandRepository, memberQueryRepository);
    }
}
