package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.TimeSlot;

class FakeTimeSlotRepositoryTest {

    private FakeTimeSlotRepository reservationTimeRepository;
    private FakeReservationRepository reservationRepository;

    public FakeTimeSlotRepositoryTest() {
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeTimeSlotRepository(reservationRepository);
    }

    @Test
    void 예약_시간_생성() {
        // given
        TimeSlot time = TimeSlot.createWithoutId(LocalTime.of(9, 0));
        // when
        TimeSlot savedTime = reservationTimeRepository.save(time);
        // then
        List<TimeSlot> allTimes = reservationTimeRepository.findAll();
        assertThat(allTimes).hasSize(1);
        assertThat(allTimes.getFirst().getId()).isEqualTo(savedTime.getId());
    }

    @Test
    void 예약_시간_삭제() {
        // given
        TimeSlot time = TimeSlot.createWithoutId(LocalTime.of(9, 0));
        TimeSlot savedTime = reservationTimeRepository.save(time);
        // when
        reservationTimeRepository.deleteById(savedTime.getId());
        // then
        List<TimeSlot> allTimes = reservationTimeRepository.findAll();
        assertThat(allTimes).hasSize(0);
    }

    @Test
    void id로_예약_시간_조회() {
        // given
        TimeSlot time = TimeSlot.createWithoutId(LocalTime.of(9, 0));
        TimeSlot savedTime = reservationTimeRepository.save(time);
        // when
        Optional<TimeSlot> optionalTime = reservationTimeRepository.findById(savedTime.getId());
        // then
        assertThat(optionalTime).isPresent();
        assertThat(optionalTime.get().getId()).isEqualTo(savedTime.getId());
    }
}