package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byDate;
import static roomescape.infrastructure.ReservationSpecs.byThemeId;
import static roomescape.infrastructure.ReservationSpecs.byTimeSlotId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.timeslot.AvailableTimeSlot;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.exception.InUseException;

@Service
@AllArgsConstructor
public class TimeSlotService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Transactional
    public TimeSlot register(final LocalTime startAt) {
        var timeSlot = new TimeSlot(startAt);
        return timeSlotRepository.save(timeSlot);
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> findAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    @Transactional
    public void removeById(final long id) {
        if (reservationRepository.exists(byTimeSlotId(id))) {
            throw new InUseException("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
        }

        var timeSlot = timeSlotRepository.getById(id);
        timeSlotRepository.delete(timeSlot);
    }

    @Transactional(readOnly = true)
    public List<AvailableTimeSlot> findAvailableTimeSlots(final LocalDate date, final long themeId) {
        var byDateAndTheme = Specification.allOf(byDate(date), byThemeId(themeId));
        var filteredReservations = reservationRepository.findAll(byDateAndTheme);
        var filteredTimeSlots = filteredReservations.stream()
                .map(Reservation::dateTime)
                .map(ReservationDateTime::timeSlot)
                .toList();

        var allTimeSlots = timeSlotRepository.findAll();
        return allTimeSlots.stream()
                .map(ts -> new AvailableTimeSlot(ts, filteredTimeSlots.contains(ts)))
                .toList();
    }
}
