package roomescape.repository;

import roomescape.domain.Waiting;

import java.util.*;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public int calculateWaitingNumber(Waiting waiting) {
        return (int) storage.values().stream()
                .filter(entry -> entry.getSlot().getId().equals(waiting.getSlot().getId()))
                .filter(entry -> entry.getId() <= waiting.getId())
                .count();
    }

    @Override
    public Waiting save(Waiting waiting) {
        long id = sequence++;
        Waiting savedWaiting = new Waiting(id, waiting.getName(), waiting.getSlot(), waiting.getWaitingNumber());
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
    public boolean isExistsBySlotId(long slotId) {
        return storage.values().stream()
                .anyMatch(entry -> entry.getSlot().getId().equals(slotId));
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

    @Override
    public Waiting findFirstBySlotId(long slotId) {
        return storage.values().stream()
                .filter(entry -> entry.getSlot().getId().equals(slotId))
                .min(Comparator.comparingInt(Waiting::getWaitingNumber))
                .orElse(null);
    }

    private boolean isSameWaiting(Waiting first, Waiting second) {
        return first.getSlot().getId().equals(second.getSlot().getId())
                && first.getName().equals(second.getName());
    }
}
