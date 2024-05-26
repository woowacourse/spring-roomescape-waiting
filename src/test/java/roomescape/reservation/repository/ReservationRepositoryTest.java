package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.time.fixture.DateTimeFixture.TODAY;
import static roomescape.time.fixture.DateTimeFixture.TOMORROW;
import static roomescape.time.fixture.DateTimeFixture.YESTERDAY;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationWithWaiting;

// 예약: 멤버 1번, 내일, 첫번째 시간, 테마 1번, 예약 완료);
// 예약: 멤버 2번, 내일, 두번째 시간, 테마 2번, 예약 완료);
// 예약: 멤버 3번, 내일, 첫번째 시간, 테마 2번, 예약 완료);
// 예약: 멤버 2번, 내일, 첫번째 시간, 테마 1번, 예약 대기 2번째);
// 예약: 멤버 3번, 내일, 첫번째 시간, 테마 1번, 예약 대기 3번째);

@DataJpaTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("멤버와 테마와 날짜 기간을 조건으로 예약을 조회할 수 있다")
    @Test
    void should_find_reservation_by_member_theme_period() {
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndPeriod(1L, 1L, TODAY, TOMORROW);
        assertThat(reservations).hasSize(1);
    }


    @DisplayName("특정 멤버의 예약을 조회할 수 있다")
    @Test
    void should_find_member_reservation_with_waiting_status() {
        List<ReservationWithWaiting> memberReservations = reservationRepository.findByMemberIdWithWaitingStatus(1L);
        assertThat(memberReservations).hasSize(1);
    }

    @DisplayName("날짜와 시간, 그리고 테마를 기반으로 예약을 조회할 수 있다")
    @Test
    void should_check_existence_of_reservation_when_date_and_theme_and_time_is_given() {
        assertAll(
                () -> assertThat(
                        reservationRepository.findByDateAndTimeAndTheme(TOMORROW, 1L, 1L)).hasSize(3),
                () -> assertThat(
                        reservationRepository.findByDateAndTimeAndTheme(YESTERDAY, 1L, 1L)).hasSize(0)
        );
    }

    @DisplayName("앞선 예약이 존재해 대기 중인 예약들을 조회할 수 있다")
    @Test
    void should_find_reservation_on_waiting() {
        List<Reservation> waitingReservations = reservationRepository.findReservationOnWaiting();

        assertThat(waitingReservations).hasSize(2);
    }
}
