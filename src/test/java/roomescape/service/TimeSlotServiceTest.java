package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.TimePatchRequest;
import roomescape.controller.dto.TimeRequest;
import roomescape.domain.TimeSlot;
import roomescape.repository.FakeTimeSlotRepository;
import roomescape.service.dto.AvailableTimeSlot;

class TimeSlotServiceTest {

    private TimeSlotService timeSlotService;

    @BeforeEach
    void setUp() {
        timeSlotService = new TimeSlotService(new FakeTimeSlotRepository());
    }

    @Test
    @DisplayName("시간 정보를 입력하여 새로운 예약 시간을 생성하고 반환한다.")
    void saveTime() {
        TimeSlot timeSlot = timeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        assertThat(timeSlot.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제하면 전체 목록에서 사라진다.")
    void removeTime() {
        TimeSlot timeSlot = timeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        timeSlotService.removeTime(timeSlot.getId());
        assertThat(timeSlotService.allTimes()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회하여 반환한다.")
    void allTimes() {
        timeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        List<TimeSlot> timeSlots = timeSlotService.allTimes();
        assertThat(timeSlots).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 시간 객체를 조회한다.")
    void findTime() {
        TimeSlot saved = timeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        TimeSlot found = timeSlotService.findTimeSlotById(saved.getId());
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시간의 전체 정보를 수정(PUT)하고 반환한다.")
    void putTime() {
        TimeSlot saved = timeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        TimeSlot updated = timeSlotService.putTime(saved.getId(), new TimeRequest(LocalTime.of(14, 0)));
        assertThat(updated.getStartAt()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("예약 시간의 일부 정보를 수정(PATCH)하고 반환한다.")
    void patchTime() {
        TimeSlot saved = timeSlotService.saveTime(new TimeRequest(LocalTime.of(10, 0)));
        TimeSlot updated = timeSlotService.patchTime(saved.getId(), new TimePatchRequest(LocalTime.of(16, 0)));
        assertThat(updated.getStartAt()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("테마와 날짜를 기준으로 이용 가능한 예약 시간 목록을 조회한다.")
    void findAvailableTimes() {
        List<AvailableTimeSlot> availableTimes = timeSlotService.findAvailableTimes(1L, LocalDate.now().plusDays(1));
        assertThat(availableTimes).isNotNull();
    }
}
