package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Reservation;
import roomescape.test.RepositoryTest;

class ReservationRepositoryTest extends RepositoryTest {
    private static final int COUNT_OF_RESERVATION = 4;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("전체 예약을 조회할 수 있다.")
    @Test
    void findAllTest() {
        List<Reservation> actual = reservationRepository.findAll();

        assertThat(actual).hasSize(COUNT_OF_RESERVATION);
    }

    @DisplayName("id로 예약을 조회할 수 있다.")
    @Test
    void findByIdTest() {
        Optional<Reservation> actual = reservationRepository.findById(1L);

        assertThat(actual.get().getId()).isEqualTo(1L);
    }

    @DisplayName("일부 조건에 따라 예약을 조회할 수 있다.")
    @Test
    void findAllByCondition_whenSomeCondition() {
        Long themeId = 1L;

        List<Reservation> actual = reservationRepository.findAllByCondition(null, themeId, null, null);

        assertThat(actual).hasSize(3);
    }

    @DisplayName("모든 조건에 따라 예약을 조회할 수 있다.")
    @Test
    void findAllByCondition_whenAllCondition() {
        Long memberId = 2L;
        Long themeId = 1L;
        LocalDate startDate = LocalDate.of(2022, 5, 5);
        LocalDate endDate = LocalDate.of(2022, 5, 5);

        List<Reservation> actual = reservationRepository.findAllByCondition(memberId, themeId, startDate, endDate);

        assertThat(actual).hasSize(1);
    }

    @DisplayName("멤버의 id로 예약을 조회할 수 있다.")
    @Test
    void findByMemberIdTest() {
        Long memberId = 2L;

        List<Reservation> actual = reservationRepository.findByMember_id(memberId);

        assertThat(actual).hasSize(2);
    }

    @DisplayName("예약을 삭제할 수 있다.")
    @Test
    void deleteByIdTest() {
        reservationRepository.deleteById(4L);

        Optional<Reservation> savedReservation = reservationRepository.findById(4L);
        assertThat(savedReservation).isEmpty();
    }
}
