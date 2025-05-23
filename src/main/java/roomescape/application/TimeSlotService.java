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
        List<TimeSlot> reservedTimeSlots = findReservedTimeSlots(date, themeId);
        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();

        return allTimeSlots.stream()
                .map(ts -> new AvailableTimeSlot(ts, reservedTimeSlots.contains(ts)))
                .toList();
    }

    private List<TimeSlot> findReservedTimeSlots(LocalDate date, long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);

        return reservations.stream()
                .map(Reservation::timeSlot)
                .toList();
    }

    public void removeById(final long id) {
        validateTimSlotNotInUse(id);
        validateTimeSlotExists(id);

        timeSlotRepository.deleteById(id);
    }

    private void validateTimSlotNotInUse(long id) {
        boolean isTimeSlotInUse = reservationRepository.existsByTimeSlotId(id);

        if (isTimeSlotInUse) {
            throw new InUseException("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
        }
    }

    private void validateTimeSlotExists(long id) {
        boolean isTimeSlotExists = timeSlotRepository.existsById(id);

        if (!isTimeSlotExists) {
            throw new NotFoundException("존재하지 않는 타임슬롯입니다.");
        }
    }
}
