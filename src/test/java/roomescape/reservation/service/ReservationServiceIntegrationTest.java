package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.reservation.fixture.ReservationFixture.waitReservation;

import java.util.List;
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
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.JdbcReservationSlotRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.reservation.service.dto.ReservationSaveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.JdbcReservationTimeRepository;

@JdbcTest
@Import({ReservationService.class, JdbcReservationSlotRepository.class,
    JdbcReservationRepository.class,
    JdbcReservationTimeRepository.class,
    JdbcReservationDateRepository.class, JdbcThemeRepository.class})
class ReservationServiceIntegrationTest {

    @Autowired
    private JdbcReservationSlotRepository reservationSlotRepository;

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
        reservationService = new ReservationService(reservationSlotRepository,
            reservationRepository, reservationTimeRepository, reservationDateRepository,
            themeRepository);
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

            reservationRepository.save(reservation(name1, date, time, theme));
            reservationRepository.save(
                waitReservation(name2, date, time, theme));
            reservationRepository.save(
                waitReservation(name3, date, time, theme));

            // when
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name3);

            // then
            assertThat(actual.getFirst().waitingTurn())
                .isEqualTo(2);
        }


        @Test
        @DisplayName("reserved 상태인 예약은 순번을 가지고 있지 않다.")
        void 성공2() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(reservation(name1, date, time, theme));

            // when
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(name1);

            // then
            assertThat(actual.getFirst().waitingTurn())
                .isNull();
        }

        @Test
        @DisplayName("예약이 취소되면 해당 슬롯의 대기 순번이 재정렬된다")
        void 성공3() {
            // given
            String themeName = "테마1";
            List<String> names = List.of("user1", "user2", "user3");

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(reservation(names.get(0), date, time, theme));
            Reservation firstWaiting = reservationRepository.save(waitReservation(names.get(1), date, time, theme));
            reservationRepository.save(waitReservation(names.get(2), date, time, theme));
            Long beforeWaitingTurn = reservationService.readAllByName(names.get(2))
                .getFirst()
                .waitingTurn();

            // when
            reservationService.cancel(firstWaiting.getId(), names.get(1));
            List<ReservationWithWaitingTurn> actual = reservationService.readAllByName(
                names.get(2));

            // then
            assertAll(
                () -> assertThat(beforeWaitingTurn).isEqualTo(2),
                () -> assertThat(actual.getFirst().waitingTurn())
                    .isEqualTo(1)
            );
        }
    }

    @Nested
    @DisplayName("reserve 메서드는")
    class ReserveTest {


        @Test
        @DisplayName("같은 사람의 중복 슬롯에 대한 예약 요청 시 예외를 반환한다")
        void 실패() {
            // given
            String themeName = "테마1";
            String name1 = "사람1";

            ReservationTime time = saveTime();
            ReservationDate date = saveDate();
            Theme theme = saveTheme(themeName);

            reservationRepository.save(reservation(name1, date, time, theme));
            ReservationSaveCommand command = new ReservationSaveCommand(date.getId(), time.getId(),
                theme.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.reserve(name1, command))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorInformation.RESERVATION_ALREADY_BOOKED.getMessage());
        }
    }

}
