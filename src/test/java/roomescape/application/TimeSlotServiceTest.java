package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.exception.InUseException;
import roomescape.exception.NotFoundException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TimeSlotServiceTest {

    @Autowired
    private TimeSlotService service;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Test
    @DisplayName("새로운 시간대를 등록할 수 있다.")
    void saveTimeSlotTest() {
        // given
        var startAt = LocalTime.of(16, 0);

        // when
        TimeSlot created = service.saveTimeSlot(startAt);

        // then
        var timeSlots = timeSlotRepository.findAll();
        assertThat(timeSlots).contains(created);
    }

    @Test
    @DisplayName("모든 시간대를 조회할 수 있다.")
    void findAllTimeSlotsTest() {
        // when
        var timeSlots = service.findAllTimeSlots();

        // then
        assertThat(timeSlots).hasSize(3);
    }

    @Test
    @DisplayName("시간대를 삭제할 수 있다.")
    void removeByIdTest() {
        // given
        var timeSlotId = 3L;

        // when
        service.removeById(timeSlotId);

        // then
        var timeSlots = service.findAllTimeSlots();
        assertThat(timeSlots).hasSize(2);
    }

    @Test
    @DisplayName("예약이 있는 시간대 삭제 시 예외를 던진다.")
    void removeById_WhenTimeSlotInUse() {
        // given
        var timeSlotId = 1L;

        // when & then
        assertThatThrownBy(() -> service.removeById(timeSlotId))
                .isInstanceOf(InUseException.class)
                .hasMessage("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 시간 삭제 시 예외를 던진다.")
    void removeById_WhenTimeSlotNotExists() {
        // given
        var notExistedTimeSlotId = 100000000000L;

        // when & then
        assertThatThrownBy(() -> service.removeById(notExistedTimeSlotId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 타임슬롯입니다.");
    }
}
