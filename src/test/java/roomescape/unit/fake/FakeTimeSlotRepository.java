package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;

public class FakeTimeSlotRepository implements TimeSlotRepository {

    private final List<TimeSlot> timeSlots = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);
    private final ReservationRepository reservationRepository;

    public FakeTimeSlotRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<TimeSlot> findAll() {
        return new ArrayList<>(timeSlots);
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        TimeSlot timeSlotWithId = new TimeSlot(index.getAndIncrement(),
                timeSlot.getStartAt());
        timeSlots.add(timeSlotWithId);
        return timeSlotWithId;
    }

    @Override
    public void deleteById(Long id) {
        timeSlots.removeIf(reservationTime -> reservationTime.getId().equals(id));
    }

    @Override
    public Optional<TimeSlot> findById(Long id) {
        return timeSlots.stream()
                .filter(reservationTime -> reservationTime.getId().equals(id))
                .findFirst();
    }
}
