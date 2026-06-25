package roomescape.repository;

import roomescape.domain.Waiting;

import java.util.*;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public int calculateWaitingNumber(Waiting waiting) {
        return (int) storage.values().stream()
                .filter(entry -> entry.getSession().getId().equals(waiting.getSession().getId()))
                .filter(entry -> entry.getId() <= waiting.getId())
                .count();
    }

    @Override
    public Waiting save(Waiting waiting) {
        long id = sequence++;
        Waiting savedWaiting = new Waiting(id, waiting.getName(), waiting.getSession(), waiting.getWaitingNumber());
        storage.put(id, savedWaiting);
        return savedWaiting;
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
    public boolean isExistsBySessionId(long slotId) {
        return storage.values().stream()
                .anyMatch(entry -> entry.getSession().getId().equals(slotId));
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
    public Waiting findFirstBySessionId(long slotId) {
        return storage.values().stream()
                .filter(entry -> entry.getSession().getId().equals(slotId))
                .min(Comparator.comparingInt(Waiting::getWaitingNumber))
                .orElse(null);
    }

    @Override
    public List<Waiting> findAllBySessionId(long sessionId) {
        return storage.values().stream()
                .filter(entry -> entry.getSession().getId().equals(sessionId))
                .sorted(Comparator.comparingInt(Waiting::getWaitingNumber))
                .toList();
    }

    private boolean isSameWaiting(Waiting first, Waiting second) {
        return first.getSession().getId().equals(second.getSession().getId())
                && first.getName().equals(second.getName());
    }
}
