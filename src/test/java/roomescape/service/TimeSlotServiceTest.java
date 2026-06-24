package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeTimeSlotRepository;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSlotServiceTest {

    private TimeSlotService reservationTimeSlotService;

    @BeforeEach
    void setUp() {
        FakeTimeSlotRepository fakeTimeRepository = new FakeTimeSlotRepository();
        FakeThemeRepository fakeThemeRepository = new FakeThemeRepository();
        FakeReservationRepository fakeReservationRepository = new FakeReservationRepository();
        reservationTimeSlotService = new TimeSlotService(fakeTimeRepository, fakeThemeRepository,
                fakeReservationRepository);
    }

    @Test
    @DisplayName("시간 정보를 입력하여 새로운 예약 시간을 생성하고 반환한다.")
    void 예약_시간_저장() {
        TimeSlot timeSlot = reservationTimeSlotService.saveTime(LocalTime.of(10, 0));
        assertThat(timeSlot.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제하면 전체 목록에서 사라진다.")
    void 예약_시간_삭제() {
        TimeSlot timeSlot = reservationTimeSlotService.saveTime(LocalTime.of(10, 0));
        reservationTimeSlotService.removeTime(timeSlot.getId());
        assertThat(reservationTimeSlotService.findAllTimes()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회하여 반환한다.")
    void 전체_예약_시간_조회() {
        reservationTimeSlotService.saveTime(LocalTime.of(10, 0));
        List<TimeSlot> timeSlots = reservationTimeSlotService.findAllTimes();
        assertThat(timeSlots).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 시간 객체를 조회한다.")
    void 식별자로_예약_시간_조회() {
        TimeSlot savedTimeSlot = reservationTimeSlotService.saveTime(LocalTime.of(10, 0));
        TimeSlot foundTimeSlot = reservationTimeSlotService.getTimeSlotById(savedTimeSlot.getId());
        assertThat(foundTimeSlot.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }
}
