package roomescape.support.fake;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;

public class FakeTimeSlotRepository implements TimeSlotRepository {

    private final List<TimeSlot> times = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return times.stream()
                .anyMatch(time -> time.startAt().equals(startAt));
    }

    @Override
    public TimeSlot save(final TimeSlot timeSlot) {
        final TimeSlot newTimeSlot = new TimeSlot(index++, timeSlot.startAt());
        times.add(newTimeSlot);
        return newTimeSlot;
    }

    @Override
    public void deleteById(final long id) {
        final TimeSlot timeSlot = findById(id).orElseThrow();
        times.remove(timeSlot);
    }

    @Override
    public List<TimeSlot> findAll() {
        return times;
    }

    @Override
    public Optional<TimeSlot> findById(final long id) {
        final TimeSlot timeSlot = times.stream()
                .filter(time -> time.id() == id)
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(timeSlot);
    }
}
