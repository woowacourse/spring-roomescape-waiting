package roomescape.fixture.config;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.global.util.DateTimeService;
import roomescape.member.application.MemberService;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationSlotRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeService;
import roomescape.theme.infrastructure.ThemeRepository;

@TestConfiguration
public class TestConfig {

    @Bean
    public DateTimeService dateTimeService() {
        return new DateTimeService(Clock.fixed(Instant.parse("2000-01-01T00:00:00Z"), ZoneOffset.UTC));
    }

    @Bean
    public ReservationTimeService reservationTimeService(final ReservationTimeRepository reservationTimeRepository) {
        return new ReservationTimeService(reservationTimeRepository);
    }

    @Bean
    public ThemeService themeService(final ThemeRepository themeRepository, final DateTimeService dateTimeService) {
        return new ThemeService(themeRepository, dateTimeService);
    }

    @Bean
    public ReservationService reservationService(final ReservationRepository reservationRepository,
                                                 final ReservationTimeRepository reservationTimeRepository,
                                                 final ReservationSlotRepository reservationSlotRepository,
                                                 final ThemeRepository themeRepository,
                                                 final MemberRepository memberRepository,
                                                 final DateTimeService dateTimeService) {
        return new ReservationService(reservationRepository, reservationSlotRepository, reservationTimeRepository,
                themeRepository, memberRepository, dateTimeService);
    }

    @Bean
    public MemberService memberService(final MemberRepository memberRepository) {
        return new MemberService(memberRepository);
    }
}
