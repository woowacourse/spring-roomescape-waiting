package roomescape.application;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_USER;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DateUtils;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.exception.InUseException;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TimeSlotServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    private TimeSlotService service;

    @BeforeEach
    void setUp() {
        service = new TimeSlotService(reservationRepository, timeSlotRepository);
    }

    @Test
    @DisplayName("예약 시간을 추가할 수 있다.")
    void registerTimeSlot() {
        // given
        var startAt = LocalTime.of(11, 0);

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
        var startAt = LocalTime.of(11, 0);
        var target = service.register(startAt);

        // when
        service.removeById(target.id());

        // then
        var timeSlots = service.findAllTimeSlots();
        assertThat(timeSlots).doesNotContain(target);
    }

    @Test
    @DisplayName("예약 시간을 삭제할 때 해당 시간에 대한 예약이 존재하면 예외 발생")
    void deleteTimeSlotWithReservation() {
        // given
        var timeSlotToBeRemoved = service.register(LocalTime.of(10, 0));
        var reservationWithTheTimeSlot = Reservation.ofExisting(1L, JUNK_USER, DateUtils.tomorrow(),
                timeSlotToBeRemoved, JUNK_THEME);
        reservationRepository.save(reservationWithTheTimeSlot);

        // when & then
        assertThatThrownBy(() -> service.removeById(timeSlotToBeRemoved.id()))
                .isInstanceOf(InUseException.class)
                .hasMessage("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
    }
}
