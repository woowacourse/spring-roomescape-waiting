package roomescape.integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.RoomescapeException;
import roomescape.timeslot.application.dto.TimeSlotAvailabilityInfo;
import roomescape.timeslot.application.dto.TimeSlotCreateCommand;
import roomescape.timeslot.application.dto.TimeSlotInfo;
import roomescape.timeslot.application.service.TimeSlotService;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class TimeSlotServiceIntegrationTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TimeSlotService timeSlotService;

    @DisplayName("이미 존재하는 예약시간을 추가하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenCreateDuplicatedTime() {
        // given
        final TimeSlotCreateCommand request = new TimeSlotCreateCommand(LocalTime.of(10, 0));
        // when & then
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 시간을 추가할 수 있다")
    @Test
    void createTimeSlot() {
        // given
        final LocalTime time = LocalTime.of(17, 0);
        final TimeSlotCreateCommand request = new TimeSlotCreateCommand(time);
        // when
        final TimeSlotInfo result = timeSlotService.createTimeSlot(request);
        // then
        final TimeSlot savedTime = timeSlotRepository.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(4L),
                () -> assertThat(result.startAt()).isEqualTo(time),
                () -> assertThat(savedTime.id()).isEqualTo(4L),
                () -> assertThat(savedTime.startAt()).isEqualTo(time)
        );
    }

    @DisplayName("모든 예약 시간을 조회할 수 있다")
    @Test
    void getTimeSlots() {
        // when
        final List<TimeSlotInfo> result = timeSlotService.findTimeSlots();
        // then
        assertThat(result).hasSize(3);
    }

    @DisplayName("예약이 존재하는 시간을 삭제할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteTimeWithinReservation() {
        // when & then
        assertThatThrownBy(() -> timeSlotService.deleteTimeSlotById(1L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @DisplayName("id를 기반으로 예약 시간을 삭제할 수 있다")
    @Test
    void deleteTimeSlotById() {
        // when
        timeSlotService.deleteTimeSlotById(3L);
        // then
        final List<TimeSlot> times = timeSlotRepository.findAll();
        assertThat(times).hasSize(2);
    }

    @DisplayName("주어진 날짜와 테마에 시간 정보를 예약 가능 여부와 함께 조회할 수 있다")
    @Test
    void findAvailableTimes() {
        // when
        final LocalDate date = LocalDate.of(2025, 4, 24);
        final List<TimeSlotAvailabilityInfo> result = timeSlotService.findAvailableTimeSlots(date, 7L);
        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).contains(
                        new TimeSlotAvailabilityInfo(1L, LocalTime.of(10, 0), true),
                        new TimeSlotAvailabilityInfo(2L, LocalTime.of(15, 0), false),
                        new TimeSlotAvailabilityInfo(3L, LocalTime.of(16, 0), false)
                )
        );
    }
}
