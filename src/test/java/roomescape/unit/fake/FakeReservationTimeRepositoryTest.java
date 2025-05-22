package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;

class FakeReservationTimeRepositoryTest {

    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeReservationRepository reservationRepository;

    public FakeReservationTimeRepositoryTest() {
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository(reservationRepository);
    }

    @Test
    void 예약_시간_생성() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        // when
        ReservationTime savedTime = reservationTimeRepository.save(time);
        // then
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        assertThat(allTimes).hasSize(1);
        assertThat(allTimes.getFirst().getId()).isEqualTo(savedTime.getId());
    }

    @Test
    void 예약_시간_삭제() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);
        // when
        reservationTimeRepository.deleteById(savedTime.getId());
        // then
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        assertThat(allTimes).hasSize(0);
    }

    @Test
    void id로_예약_시간_조회() {
        // given
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);
        // when
        Optional<ReservationTime> optionalTime = reservationTimeRepository.findById(savedTime.getId());
        // then
        assertThat(optionalTime).isPresent();
        assertThat(optionalTime.get().getId()).isEqualTo(savedTime.getId());
    }
}