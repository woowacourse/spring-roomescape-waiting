package roomescape.timeslot.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.ThemeRepository;
import roomescape.timeslot.application.dto.CreateTimeSlotRequest;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.user.domain.UserRepository;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TimeSlotCommandServiceTest {

    @Autowired
    private ReservationTimeCommandService reservationTimeCommandService;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("예약 시간을 생성할 수 있다")
    void createReservationTime() {
        // given
        final CreateTimeSlotRequest request = new CreateTimeSlotRequest(ReservationTime.from(LocalTime.of(12, 30)));

        // when
        final TimeSlot timeSlot = reservationTimeCommandService.create(request);

        // then
        assertThat(timeSlot.getStartAt().getValue()).isEqualTo(LocalTime.of(12, 30));
        assertThat(timeSlotRepository.findById(timeSlot.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void deleteReservationTime() {
        // given
        final TimeSlot saved =
                timeSlotRepository.save(
                        TimeSlot.withoutId(ReservationTime.from(LocalTime.of(14, 0))));
        final TimeSlotId id = saved.getId();

        // when
        reservationTimeCommandService.delete(id);

        // then
        assertThat(timeSlotRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservationTime() {
        // given
        final TimeSlotId id = TimeSlotId.from(-1L);

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[TIME_SLOT] not found. params={TimeSlotId=TimeSlotId(-1)}");
    }

    @Test
    @DisplayName("추가하려는 시간이 이미 존재한다면, 예외가 발생한다")
    void existsTime() {
        // given
        final LocalTime time = LocalTime.of(14, 0);
        timeSlotRepository.save(TimeSlot.withoutId(ReservationTime.from(time)));

        final CreateTimeSlotRequest sameTimeRequest = new CreateTimeSlotRequest(ReservationTime.from(time));

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.create(sameTimeRequest))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("TIME_SLOT already exists. params={ReservationTime=ReservationTime(value=14:00)}");
    }
}
