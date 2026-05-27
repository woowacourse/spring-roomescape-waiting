package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorInformation;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationChangeCommand;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.JdbcReservationTimeRepository;

import java.util.List;

@JdbcTest
@Import({ReservationService.class, JdbcReservationRepository.class,
    JdbcReservationTimeRepository.class,
    JdbcReservationDateRepository.class, JdbcThemeRepository.class})
class ReservationServiceIntegrationTest {

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcReservationDateRepository reservationDateRepository;

    @Autowired
    private JdbcReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository,
            reservationTimeRepository, reservationDateRepository, themeRepository);
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


    @Nested
    @DisplayName("getMyReservations 메서드는")
    class GetMyReservationsTest {


        @Test
        @DisplayName("내 예약을 조회한다")
        void 성공1() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";
            String name2 = "사람2";
            String name3 = "사람3";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));
            reservationRepository.save(
                ReservationFixture.waitReservation(name2, date, time, theme));
            reservationRepository.save(
                ReservationFixture.waitReservation(name3, date, time, theme));

            // when
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name3);

            // then
            assertThat(actual.getFirst().waitingTurn())
                .isEqualTo(2);
        }


        @Test
        @DisplayName("getMyReservations no waiting turn")
        void 성공2() {
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
            assertThat(actual.getFirst().waitingTurn())
                .isNull();
        }
    }

    @Nested
    @DisplayName("reserve 메서드는")
    class ReserveTest {


        @Test
        @DisplayName("중복된")
        void 실패() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(ReservationFixture.reservation(name1, date, time, theme));
            ReservationSaveCommand command = new ReservationSaveCommand(date.getId(), time.getId(),
                theme.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name1, command))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
        }
    }

    @Nested
    @DisplayName("waiting 메서드는")
    class WaitingTest {


        @Test
        @DisplayName("waiting reserve not changeable")
        void 실패() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            Reservation savedReservation = reservationRepository.save(
                ReservationFixture.waitReservation(name1, date, time, theme));
            ReservationChangeCommand command = new ReservationChangeCommand(
                savedReservation.getId(),
                savedReservation.getName(), savedReservation.getDate().getId(),
                savedReservation.getTime().getId());

            // when & then
            assertThatThrownBy(() -> reservationService.changeSchedule(command))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_WAITING.getMessage());
        }
    }
}
