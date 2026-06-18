package roomescape.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import roomescape.domain.Time;
import roomescape.repository.TimeRepository;

public class FakeTimeDao implements TimeRepository {

    private final Map<Long, Time> storage = new HashMap<>();
    private long sequence = 1L;

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
        long id = sequence++;
        Time savedTime = new Time(id, time.getStartAt());
        storage.put(id, savedTime);
        return savedTime;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }
}
