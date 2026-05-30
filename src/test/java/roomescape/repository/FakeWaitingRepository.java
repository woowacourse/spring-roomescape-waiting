package roomescape.repository;

import roomescape.domain.Waiting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public int calculateWaitingNumber(Waiting waiting) {
        return (int) storage.values().stream()
                .filter(entry -> isSameSchedule(entry, waiting))
                .filter(entry -> entry.getId() <= waiting.getId())
                .count();
    }

    @Override
    public Waiting save(Waiting waiting) {
        long id = sequence++;
        Waiting savedWaiting = createSavedWaiting(id, waiting);
        storage.put(id, savedWaiting);
        return waiting;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public boolean isExists(Waiting waiting) {
        return storage.values().stream()
                .anyMatch(entry -> isSameWaiting(entry, waiting));
    }

    @Override
    public List<Waiting> findByName(String name) {
        return storage.values().stream()
                .filter(entry -> entry.getName().equals(name))
                .toList();
    }

    @Override
    public Optional<Waiting> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    private Waiting createSavedWaiting(long id, Waiting waiting) {
        return new Waiting(
                id,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTimeSlot(),
                waiting.getTheme(),
                waiting.getWaitingNumber()
        );
    }

    private boolean isSameSchedule(Waiting first, Waiting second) {
        return first.getDate().equals(second.getDate())
                && first.getTimeSlot().getId().equals(second.getTimeSlot().getId())
                && first.getTheme().getId().equals(second.getTheme().getId());
    }

    private boolean isSameWaiting(Waiting first, Waiting second) {
        return isSameSchedule(first, second)
                && first.getName().equals(second.getName());
    }
}
