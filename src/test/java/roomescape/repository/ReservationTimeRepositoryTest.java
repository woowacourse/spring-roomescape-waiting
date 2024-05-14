package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

@Transactional
class ReservationTimeRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;


    @DisplayName("예약 시간 저장")
    @Test
    void save() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.parse("01:00"));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        assertThat(savedReservationTime.getStartAt()).isEqualTo(LocalTime.parse("01:00"));
    }

    @DisplayName("존재하는 예약 시간 조회")
    @Test
    void findExistById() {
        ReservationTime findedReservationTime = reservationTimeRepository.findById(1L).orElseThrow();

        assertThat(findedReservationTime.getStartAt()).isEqualTo(LocalTime.parse("09:00"));
    }

    @DisplayName("존재하지 않는 예약 시간 조회")
    @Test
    void findEmptyById() {
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(100L);
        assertThat(reservationTime).isEmpty();
    }

    @DisplayName("존재하는 예약 시간 삭제")
    @Test
    void deleteExistById() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.parse("01:00"));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        assertThatCode(() -> reservationTimeRepository.deleteById(savedReservationTime.getId()))
                .doesNotThrowAnyException();
    }

    @DisplayName("특정 시간 존재 여부 확인")
    @Test
    void existByStartAtTrue() {
        boolean existByStartAt = reservationTimeRepository.existByStartAt(LocalTime.parse("10:00"));

        assertThat(existByStartAt).isTrue();
    }

    @DisplayName("특정 시간 존재 여부 확인")
    @Test
    void existByStartAtFalse() {
        boolean existByStartAt = reservationTimeRepository.existByStartAt(LocalTime.parse("06:00"));

        assertThat(existByStartAt).isFalse();
    }
}
