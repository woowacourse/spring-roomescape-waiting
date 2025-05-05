package roomescape.fixture.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.member.domain.MemberQueryRepository;
import roomescape.member.infrastructure.JdbcMemberRepository;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationQueryRepository;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.domain.ReservationTimeQueryRepository;
import roomescape.reservation.infrastructure.JdbcReservationRepository;
import roomescape.reservation.infrastructure.JdbcReservationTimeRepository;
import roomescape.theme.applcation.ThemeService;
import roomescape.theme.domain.ThemeCommandRepository;
import roomescape.theme.domain.ThemeQueryRepository;
import roomescape.theme.infrastructure.JdbcThemeRepository;

@TestConfiguration
public class TestConfig {

    @Bean
    public ReservationTimeCommandRepository reservationTimeCommandRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Bean
    public ReservationTimeQueryRepository reservationTimeQueryRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @Bean
    public ThemeCommandRepository themeCommandRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcThemeRepository(jdbcTemplate);
    }

    @Bean
    public ThemeQueryRepository themeQueryRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcThemeRepository(jdbcTemplate);
    }

    @Bean
    public MemberCommandRepository memberCommandRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcMemberRepository(jdbcTemplate);
    }

    @Bean
    public MemberQueryRepository memberQueryRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcMemberRepository(jdbcTemplate);
    }

    @Bean
    public ReservationCommandRepository reservationCommandRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcReservationRepository(jdbcTemplate);
    }

    @Bean
    public ReservationQueryRepository reservationQueryRepository(
            final JdbcTemplate jdbcTemplate
    ) {
        return new JdbcReservationRepository(jdbcTemplate);
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
}
