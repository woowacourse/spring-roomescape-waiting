package roomescape.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.ReservationTimeService;
import roomescape.application.reservation.ThemeService;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.infrastructure.JdbcSupportTest;
import roomescape.infrastructure.persistance.JdbcMemberRepository;
import roomescape.infrastructure.persistance.JdbcReservationRepository;
import roomescape.infrastructure.persistance.JdbcReservationTimeRepository;
import roomescape.infrastructure.persistance.JdbcThemeRepository;

public abstract class ServiceIntegrationTest extends JdbcSupportTest {

    protected static final Clock clock = Clock.fixed(
            Instant.parse("2025-05-11T00:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    protected static final MemberRepository memberRepository = new JdbcMemberRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE)
    );
    protected static final ThemeRepository themeRepository = new JdbcThemeRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE), TEST_DATASOURCE
    );
    protected static final ReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE), TEST_DATASOURCE
    );
    protected static final ReservationRepository reservationRepository = new JdbcReservationRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE), TEST_DATASOURCE
    );

    protected static final ThemeService themeService = new ThemeService(themeRepository, reservationRepository, clock);
    protected static final ReservationTimeService reservationTimeService = new ReservationTimeService(
            reservationTimeRepository, reservationRepository);
    protected static final ReservationService reservationService = new ReservationService(reservationTimeRepository,
            reservationRepository, themeRepository, memberRepository, clock);

}
