package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ErrorCollector;
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
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationSaveCommand;
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

    @Test
    @DisplayName("이미 슬롯에 내 예약이 있으면 예약할 수 없다.")
    void reserve_duplicated() {
        // given
        String themeName = "테마1";
        String name1 = "사람1";

        ReservationTime time = saveTime();
        ReservationDate date = saveDate();
        Theme theme = saveTheme(themeName);

        reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));
        ReservationSaveCommand command = new ReservationSaveCommand(date.getId(), time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.reserve(name1, command))
            .isInstanceOf(ReservationException.class)
            .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
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
