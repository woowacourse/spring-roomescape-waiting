package roomescape.infrastructure.fake;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.TimeSlot;
import roomescape.domain.repository.TimeSlotRepository;

public class TimeSlotFakeRepository implements TimeSlotRepository {

    private final Map<Long, TimeSlot> timeSlots = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Optional<TimeSlot> findById(final long id) {
        return Optional.ofNullable(timeSlots.get(id));
    }

    @Override
    public long save(final TimeSlot timeSlot) {
        var id = index.getAndIncrement();
        var created = new TimeSlot(id, timeSlot.startAt());
        timeSlots.put(id, created);
        return id;
    }

    @Override
    public boolean removeById(final long id) {
        TimeSlot removed = timeSlots.remove(id);
        return removed != null;
    }

    @Override
    public List<TimeSlot> findAll() {
        return List.copyOf(timeSlots.values());
    }
}
