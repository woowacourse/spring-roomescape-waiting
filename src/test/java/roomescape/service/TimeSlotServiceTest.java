package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.TimeRequest;
import roomescape.domain.TimeSlot;
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
        reservationTimeSlotService = new TimeSlotService(fakeTimeRepository, fakeThemeRepository);
    }

    @Test
    @DisplayName("시간 정보를 입력하여 새로운 예약 시간을 생성하고 반환한다.")
    void saveTime() {
        TimeSlot timeSlot = reservationTimeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        assertThat(timeSlot.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제하면 전체 목록에서 사라진다.")
    void removeTime() {
        TimeSlot timeSlot = reservationTimeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        reservationTimeSlotService.removeTime(timeSlot.getId());
        assertThat(reservationTimeSlotService.allTimes()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회하여 반환한다.")
    void allTimes() {
        reservationTimeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        List<TimeSlot> timeSlots = reservationTimeSlotService.allTimes();
        assertThat(timeSlots).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 시간 객체를 조회한다.")
    void findTime() {
        TimeSlot savedTimeSlot = reservationTimeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        TimeSlot foundTimeSlot = reservationTimeSlotService.findTimeSlotById(savedTimeSlot.getId());
        assertThat(foundTimeSlot.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }
}
