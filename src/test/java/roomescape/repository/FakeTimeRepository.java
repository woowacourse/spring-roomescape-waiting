package roomescape.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.domain.Time;

public class FakeTimeRepository implements TimeRepository {

    private final Map<Long, Time> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    public void clear() {
        storage.clear();
        sequence.set(1L);
    }

    @Override
    public List<Time> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Time> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Time save(Time time) {
        long id = sequence.getAndIncrement();
        Time savedTime = new Time(id, time.getStartAt());
        storage.put(id, savedTime);
        return savedTime;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }
}
