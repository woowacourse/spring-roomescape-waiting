package roomescape.time.repository;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.time.domain.Time;

public class FakeTimeRepository implements TimeRepository {

    private final Map<Long, Time> times = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong(0);

    public FakeTimeRepository() {
        long increasedId = id.incrementAndGet();
        times.put(increasedId, makeTime(Time.from(LocalTime.of(9, 0)), increasedId));

        increasedId = id.incrementAndGet();
        times.put(increasedId, makeTime(Time.from(LocalTime.of(10, 0)), increasedId));
    }

    @Override
    public Time save(Time time) {
        long increasedId = id.incrementAndGet();
        times.put(increasedId, makeTime(time, increasedId));
        return time;
    }

    @Override
    public List<Time> findAllByOrderByStartAt() {
        return times.values().stream()
                .sorted(Comparator.comparing(Time::getStartAt))
                .toList();
    }

    @Override
    public Optional<Time> findByStartAt(LocalTime startAt) {
        return times.values().stream()
                .filter(time -> time.getStartAt() == startAt)
                .findAny();
    }

    @Override
    public Optional<Time> findById(long id) {
        Time time = times.get(id);
        return Optional.ofNullable(time);
    }

    @Override
    public void deleteById(long timeId) {
        times.remove(timeId);
    }

    private Time makeTime(Time time, long id) {
        ReflectionTestUtils.setField(time, "id", id);
        return time;
    }
}
