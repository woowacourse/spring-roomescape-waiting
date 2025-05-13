package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.common.util.time.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.JdbcMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.infrastructure.JdbcReservationRepository;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JdbcReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JdbcThemeRepository;

class ReservationServiceTest {

    private final static DataSource DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testDatabase")
            .username("sa")
            .build();

    private ReservationService reservationService;

    @BeforeEach
    void beforeEach() {
        DatabaseInitializationSettings databaseInitializationSettings = new DatabaseInitializationSettings();
        databaseInitializationSettings.setSchemaLocations(List.of("classpath:/schema.sql"));
        DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer = new DataSourceScriptDatabaseInitializer(DATASOURCE, databaseInitializationSettings);
        dataSourceScriptDatabaseInitializer.initializeDatabase();

        DateTime dateTime = () -> LocalDateTime.of(2025, 10, 5, 10, 0);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(DATASOURCE);

        ThemeRepository themeRepository = new JdbcThemeRepository(namedParameterJdbcTemplate, DATASOURCE);
        Theme theme = Theme.createWithoutId("테스트1", "설명", "localhost:8080");
        themeRepository.save(theme);

        ReservationTimeRepository reservationTimeRepository = new JdbcReservationTimeRepository(namedParameterJdbcTemplate, DATASOURCE);
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);

        MemberRepository memberRepository = new JdbcMemberRepository(namedParameterJdbcTemplate, DATASOURCE);
        Long memberId = memberRepository.save(Member.createWithoutId("유저1", "email@email.com", "password"));
        Member member = memberRepository.findById(memberId);

        ReservationRepository reservationRepository = new JdbcReservationRepository(namedParameterJdbcTemplate, DATASOURCE);
        reservationRepository.save(Reservation.createWithoutId(LocalDate.of(2024, 10, 6), reservationTime1, theme, member));

        reservationService = new ReservationService(
                dateTime,
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약을 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource
    void cant_not_reserve_before_now(final LocalDate date, final Long timeId) {
        Assertions.assertThatThrownBy(
                        () -> reservationService.createReservation(new ReservationRequest(date, timeId, 1L), 1L))
                .isInstanceOf(ReservationException.class);
    }

    private static Stream<Arguments> cant_not_reserve_before_now() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 10, 5), 1L),
                Arguments.of(LocalDate.of(2025, 9, 5), 1L),
                Arguments.of(LocalDate.of(2025, 10, 4), 1L),
                Arguments.of(LocalDate.of(2025, 10, 5), 2L)
        );
    }

    @DisplayName("중복 예약이 불가하다.")
    @Test
    void cant_not_reserve_duplicate() {
        Assertions.assertThatThrownBy(() -> reservationService.createReservation(
                        new ReservationRequest(LocalDate.of(2024, 10, 6), 1L, 1L), 1L))
                .isInstanceOf(ReservationException.class);
    }
}
