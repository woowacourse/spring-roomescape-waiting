package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_USER;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DateUtils;
import roomescape.domain.Reservation;
import roomescape.domain.TimeSlot;
import roomescape.infrastructure.fake.ReservationFakeRepository;
import roomescape.infrastructure.fake.TimeSlotFakeRepository;

class TimeSlotServiceTest {

    private TimeSlotService service = new TimeSlotService(new ReservationFakeRepository(), new TimeSlotFakeRepository());

    @Test
    @DisplayName("예약 시간을 추가할 수 있다.")
    void registerTimeSlot() {
        // given
        var startAt = LocalTime.of(10, 0);

        // when
        TimeSlot created = service.register(startAt);

        // then
        var timeSlots = service.findAllTimeSlots();
        assertThat(timeSlots).contains(created);
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다.")
    void deleteTimeSlot() {
        // given
        var startAt = LocalTime.of(10, 0);
        var target = service.register(startAt);

        // when
        boolean isRemoved = service.removeById(target.id());

        // then
        var timeSlots = service.findAllTimeSlots();
        assertAll(
            () -> assertThat(isRemoved).isTrue(),
            () -> assertThat(timeSlots).doesNotContain(target)
        );
    }

    @Test
    @DisplayName("예약 시간을 삭제할 때 해당 시간에 대한 예약이 존재하면 예외 발생")
    void deleteTimeSlotWithReservation() {
        // given
        var reservationRepository = new ReservationFakeRepository();
        var timeSlotRepository = new TimeSlotFakeRepository();
        var timeSlotService = new TimeSlotService(reservationRepository, timeSlotRepository);

        var timeSlotToBeRemoved = timeSlotService.register(LocalTime.of(10, 0));
        var reservationWithTheTimeSlot = Reservation.ofExisting(1L, JUNK_USER, DateUtils.tomorrow(), timeSlotToBeRemoved, JUNK_THEME);
        reservationRepository.save(reservationWithTheTimeSlot);

        // when & then
        assertThatThrownBy(() -> timeSlotService.removeById(timeSlotToBeRemoved.id()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
    }
}
