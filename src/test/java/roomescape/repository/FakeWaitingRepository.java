package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.Waiting;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public void save(Waiting waiting) {
        long id = sequence++;
        Waiting savedWaiting = createSavedWaiting(id, waiting);
        storage.put(id, savedWaiting);
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
        return Optional.empty();
    }

    private Waiting createSavedWaiting(long id, Waiting waiting) {
        return new Waiting(
                id,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTimeSlotId(),
                waiting.getThemeId(),
                waiting.getWaitingNumber()
        );
    }

    private boolean isSameSchedule(Waiting first, Waiting second) {
        return first.getDate().equals(second.getDate())
                && first.getTimeSlotId().equals(second.getTimeSlotId())
                && first.getThemeId().equals(second.getThemeId());
    }

    private boolean isSameWaiting(Waiting first, Waiting second) {
        return isSameSchedule(first, second)
                && first.getName().equals(second.getName());
    }

    private boolean isTargetSchedule(Waiting entry, LocalDate date, Long timeSlotId, Long themeId) {
        return entry.getDate().equals(date)
                && entry.getTimeSlotId().equals(timeSlotId)
                && entry.getThemeId().equals(themeId);
    }
}
