package roomescape.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.AvailableTimeSlot;
import roomescape.domain.Reservation;
import roomescape.domain.TimeSlot;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.TimeSlotRepository;

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

    public TimeSlot register(final LocalTime startAt) {
        var timeSlot = new TimeSlot(startAt);
        return timeSlotRepository.save(timeSlot);
    }

    public List<TimeSlot> findAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public void removeById(final long id) {
        List<Reservation> reservations = reservationRepository.findByTimeSlotId(id);
        if (!reservations.isEmpty()) {
            throw new IllegalStateException("삭제하려는 타임 슬롯을 사용하는 예약이 있습니다.");
        }
        timeSlotRepository.deleteById(id);
    }

    public List<AvailableTimeSlot> findAvailableTimeSlots(final LocalDate date, final long themeId) {
        var filteredReservations = reservationRepository.findByDateAndThemeId(date, themeId);
        var filteredTimeSlots = filteredReservations.stream()
                .map(Reservation::timeSlot)
                .toList();

        var allTimeSlots = timeSlotRepository.findAll();

        return allTimeSlots.stream()
                .map(ts -> new AvailableTimeSlot(ts, filteredTimeSlots.contains(ts)))
                .toList();
    }
}
