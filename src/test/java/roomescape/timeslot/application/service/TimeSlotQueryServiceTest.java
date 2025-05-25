package roomescape.timeslot.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.domain.TimeSlotRepository;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TimeSlotQueryServiceTest {

    @Autowired
    private ReservationTimeQueryService reservationTimeQueryService;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Test
    @DisplayName("예약 시간을 조회할 수 있다")
    void getReservationTime() {
        // given
        final LocalTime time = LocalTime.of(10, 0);
        final TimeSlot savedTime = timeSlotRepository.save(TimeSlot.withoutId(ReservationTime.from(time)));
        final TimeSlotId id = savedTime.getId();

        // when
        final TimeSlot timeSlot = reservationTimeQueryService.get(id);

        // then
        assertThat(timeSlot.getStartAt().getValue()).isEqualTo(time);
    }

    @Test
    @DisplayName("예약 시간을 전체 조회할 수 있다")
    void getAllReservationTimes() {
        // given
        timeSlotRepository.save(TimeSlot.withoutId(ReservationTime.from(LocalTime.of(10, 0))));
        timeSlotRepository.save(TimeSlot.withoutId(ReservationTime.from(LocalTime.of(11, 0))));

        // when
        final List<TimeSlot> times = reservationTimeQueryService.getAll();

        // then
        assertThat(times).hasSize(2);
    }
}
