package roomescape.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.timeslot.AvailableTimeSlot;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.exception.InUseException;
import roomescape.exception.NotFoundException;

@Service
public class TimeSlotService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(
            final ReservationRepository reservationRepository,
            final TimeSlotRepository timeSlotRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public TimeSlot saveTimeSlot(final LocalTime startAt) {
        TimeSlot timeSlot = new TimeSlot(startAt);
        return timeSlotRepository.save(timeSlot);
    }

    public List<TimeSlot> findAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public List<AvailableTimeSlot> findAvailableTimeSlots(final LocalDate date, final long themeId) {
        List<Reservation> filteredReservations = reservationRepository.findByDateAndThemeId(date, themeId);

        List<TimeSlot> filteredTimeSlots = filteredReservations.stream()
                .map(Reservation::timeSlot)
                .toList();

        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();

        return allTimeSlots.stream()
                .map(ts -> new AvailableTimeSlot(ts, filteredTimeSlots.contains(ts)))
                .toList();
    }

    public void removeById(final long id) {
        List<Reservation> reservations = reservationRepository.findByTimeSlotId(id);
        if (!reservations.isEmpty()) {
            throw new InUseException("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
        }
        timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타임슬롯입니다."));
        timeSlotRepository.deleteById(id);
    }
}
