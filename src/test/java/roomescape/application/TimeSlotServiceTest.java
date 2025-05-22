package roomescape.application;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.DateUtils.tomorrow;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.TestRepositoryHelper;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.exception.InUseException;

@DataJpaTest
@Import({TimeSlotService.class, TestRepositoryHelper.class})
class TimeSlotServiceTest {

    @Autowired
    private TimeSlotService service;
    @Autowired
    private TestRepositoryHelper repositoryHelper;

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void registerTimeSlot() {
        // given
        var startAt = LocalTime.of(11, 0);

        // when
        var created = service.register(startAt);

        // then
        var timeSlots = service.findAllTimeSlots();
        assertThat(timeSlots).contains(created);
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void deleteTimeSlot() {
        // given
        var startAt = LocalTime.of(11, 0);
        var created = service.register(startAt);

        // when
        service.removeById(created.id());

        // then
        var timeSlots = service.findAllTimeSlots();
        assertThat(timeSlots).doesNotContain(created);
    }

    @Test
    @DisplayName("예약 시간을 삭제할 때 해당 시간에 대한 예약이 존재하면 예외가 발생한다.")
    void deleteTimeSlotWithReservation() {
        // given
        var user = repositoryHelper.saveAnyUser();
        var theme = repositoryHelper.saveAnyTheme();
        var timeSlotToBeRemoved = service.register(LocalTime.of(10, 0));

        var reservationWithTheTimeSlot = new Reservation(user, new ReservationSlot(ReservationDateTime.of(tomorrow(), timeSlotToBeRemoved), theme));
        repositoryHelper.saveReservation(reservationWithTheTimeSlot);

        // when & then
        assertThatThrownBy(() -> service.removeById(timeSlotToBeRemoved.id()))
                .isInstanceOf(InUseException.class)
                .hasMessage("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
    }
}
