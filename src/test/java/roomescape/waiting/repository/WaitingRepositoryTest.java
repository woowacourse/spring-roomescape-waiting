package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.test.RepositoryTest;
import roomescape.waiting.domain.Waiting;

class WaitingRepositoryTest extends RepositoryTest {
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("멤버 id를 통해 예약 대기를 조회할 수 있다.")
    @Test
    void findByMemberIdTest() {
        List<Waiting> waitings = waitingRepository.findByMemberId(4L);

        assertThat(waitings).hasSize(1);
    }

    @DisplayName("특정 예약의 가장 최근 예약 대기를 가져올 수 있다.")
    @Test
    void findTopByReservationIdOrderByCreatedAtAscTest() {
        Optional<Waiting> waiting = waitingRepository.findTopByReservationIdOrderByCreatedAtAsc(5L);

        assertThat(waiting.get().getId()).isEqualTo(1L);
    }

    @DisplayName("특정 예약의 가장 최근 예약 대기가 없을 경우, 빈 값을 가져온다.")
    @Test
    void findTopByReservationIdOrderByCreatedAtAscTest_whenWaitingNotExist() {
        Optional<Waiting> waiting = waitingRepository.findTopByReservationIdOrderByCreatedAtAsc(1L);

        assertThat(waiting).isEmpty();
    }

    @DisplayName("특정 시간 이전에, 몇 개의 예약 대기가 존재하는지 확인한다.")
    @Test
    void countByReservationAndCreatedAtLessThanEqualTest() {
        Reservation reservation = reservationRepository.findById(5L).get();
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 19, 9, 0, 0);

        Long order = waitingRepository.countByReservationAndCreatedAtLessThanEqual(reservation, createdAt);

        assertThat(order).isEqualTo(2);
    }

    @DisplayName("예약 아이디, 멤버 아이디에 해당하는 예약 대기가 존재하는지 확인한다. - 존재할 때")
    @Test
    void existsByReservationIdAndMemberIdTest_whenExist() {
        Long reservationId = 5L;
        Long memberId = 4L;

        boolean actual = waitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);

        assertThat(actual).isTrue();
    }

    @DisplayName("예약 아이디, 멤버 아이디에 해당하는 예약 대기가 존재하는지 확인한다. - 존재하지 않을 때")
    @Test
    void existsByReservationAndMemberTest_whenNotExist() {
        Long reservationId = 5L;
        Long memberId = 3L;

        boolean actual = waitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);

        assertThat(actual).isFalse();
    }
}
