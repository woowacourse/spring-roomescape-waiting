package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.allOf;
import static roomescape.infrastructure.ReservationSpecs.byDate;
import static roomescape.infrastructure.ReservationSpecs.byThemeId;
import static roomescape.infrastructure.ReservationSpecs.byTimeSlotId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotBookStatus;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.exception.InUseException;

@Service
@AllArgsConstructor
public class TimeSlotService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlot register(final LocalTime startAt) {
        var timeSlot = new TimeSlot(startAt);
        return timeSlotRepository.save(timeSlot);
    }

    public List<TimeSlot> findAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public void removeById(final long id) {
        if (reservationRepository.exists(byTimeSlotId(id))) {
            throw new InUseException("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
        }
        timeSlotRepository.deleteByIdOrElseThrow(id);
    }

    public List<TimeSlotBookStatus> findAvailableTimeSlots(final LocalDate date, final long themeId) {
        var reservations = reservationRepository.findAllWithWrapping(allOf(byDate(date), byThemeId(themeId)));
        var timeSlots = timeSlotRepository.findAll();
        return reservations.checkBookStatuses(timeSlots);
    }
}
