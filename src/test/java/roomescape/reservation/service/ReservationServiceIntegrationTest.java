package roomescape.reservation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.JdbcReservationTimeRepository;

import java.util.List;

@JdbcTest
@Import({JdbcReservationRepository.class, JdbcReservationTimeRepository.class, JdbcReservationDateRepository.class, JdbcThemeRepository.class})
class ReservationServiceIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcReservationDateRepository reservationDateRepository;

    @Autowired
    private JdbcReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, reservationDateRepository, themeRepository);
    }

    @Test
    @DisplayName("나의 예약 목록을 조회하면, 대기 순번을 조회한다.")
    void getMyReservations() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";
        String name2 = "사람2";
        String name3 = "사람3";

        ReservationTime time = saveTime();
        ReservationDate date = saveDate();
        Theme theme = saveTheme(themeName);


        reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));
        reservationRepository.save(ReservationFixture.waitReservation(name2, date, time, theme));
        reservationRepository.save(ReservationFixture.waitReservation(name3, date, time, theme));

        // when
        List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name3);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("나의 예약 목록을 조회할때, 예약상태면 대기 순번이 없다.")
    void getMyReservations_no_waiting_turn() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";

        ReservationTime time = saveTime();
        ReservationDate date = saveDate();
        Theme theme = saveTheme(themeName);


        reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));

        // when
        List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name1);

        // then
        Assertions.assertThat(actual.getFirst().waitingTurn())
                .isNull();
    }

    private ReservationTime saveTime() {
        return reservationTimeRepository.save(ReservationTimeFixture.activeTime15());
    }

    private ReservationDate saveDate() {
        return reservationDateRepository.save(ReservationDateFixture.activeOneWeekLater());
    }

    private Theme saveTheme(String themeName) {
        return themeRepository.save(ThemeFixture.activeTheme(themeName));
    }

}
