package roomescape.reservationTime.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.JdbcMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.JdbcReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.exception.ReservationTimeException;
import roomescape.reservationTime.infrastructure.JdbcReservationTimeRepository;
import roomescape.reservationTime.presentation.dto.TimeConditionRequest;
import roomescape.reservationTime.presentation.dto.TimeConditionResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JdbcThemeRepository;

class ReservationTimeServiceTest {

    private final static DataSource DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testDatabase")
            .username("sa")
            .build();

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void beforeEach() {
        DatabaseInitializationSettings databaseInitializationSettings = new DatabaseInitializationSettings();
        databaseInitializationSettings.setSchemaLocations(List.of("classpath:/schema.sql"));
        DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer = new DataSourceScriptDatabaseInitializer(DATASOURCE, databaseInitializationSettings);
        dataSourceScriptDatabaseInitializer.initializeDatabase();

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(DATASOURCE);

        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(11, 0));

        ThemeRepository themeRepository = new JdbcThemeRepository(namedParameterJdbcTemplate, DATASOURCE);
        Theme theme = Theme.createWithoutId("테마", "테마", "테마");
        themeRepository.save(theme);

        theme = themeRepository.findById(1L);

        ReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(namedParameterJdbcTemplate, DATASOURCE);
        Long reservationTimeId1 = reservationTimeRepository.save(reservationTime1);
        Long reservationTimeId2 = reservationTimeRepository.save(reservationTime2);

        reservationTime1 = ReservationTime.createWithId(reservationTimeId1, reservationTime1.getStartAt());
        ReservationTime.createWithId(reservationTimeId2, reservationTime2.getStartAt());

        MemberRepository memberRepository = new JdbcMemberRepository(namedParameterJdbcTemplate, DATASOURCE);
        Long memberId = memberRepository.save(Member.createWithoutId("유저1", "email@email.com", "password"));
        Member member = memberRepository.findById(memberId);

        ReservationRepository reservationRepository = new JdbcReservationRepository(namedParameterJdbcTemplate, DATASOURCE);
        reservationRepository.save(Reservation.createWithoutId(LocalDate.of(2024, 10, 6), reservationTime1, theme, member));

        reservationTimeService = new ReservationTimeService(reservationRepository, reservationTimeRepository);
    }

    @DisplayName("이미 존재하는 예약이 있는 경우 예약 시간을 삭제할 수 없다.")
    @Test
    void can_not_delete_when_reservation_exists() {
        Assertions.assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(1L))
                .isInstanceOf(ReservationTimeException.class);
    }

    @DisplayName("예약 가능 시간 조회 테스트")
    @Test
    void time_condition_test() {
        LocalDate localDate = LocalDate.of(2024, 10, 6);
        Long themeId = 1L;

        List<TimeConditionResponse> responses = reservationTimeService.getTimesWithCondition(
                new TimeConditionRequest(localDate, themeId));

        Assertions.assertThat(responses).containsExactlyInAnyOrder(
                new TimeConditionResponse(1L, LocalTime.of(10, 0), true),
                new TimeConditionResponse(2L, LocalTime.of(11, 0), false)
                );
    }
}
