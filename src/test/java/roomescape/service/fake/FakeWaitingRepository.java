package roomescape.service.fake;

import roomescape.domain.Schedule;
import roomescape.domain.Waiting;
import roomescape.repository.WaitingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitingList = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Waiting save(Waiting waiting) {
        Waiting saved = new Waiting(
                sequence.incrementAndGet(),
                waiting.getName(),
                waiting.getSchedule());
        waitingList.add(saved);
        return saved;
    }

    @Override
    public Optional<Waiting> findByScheduleAndName(Waiting waiting) {
        return waitingList.stream()
                .filter(w -> w.getSchedule().equals(waiting.getSchedule()))
                .filter(w -> w.getName().equals(waiting.getName()))
                .findFirst();
    }

    @Override
    public Optional<Waiting> findById(long id) {
        return waitingList.stream()
                .filter(w -> w.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Waiting> findUserWaitingList(String name, int page, int size) {
        return waitingList.stream()
                .filter(w -> w.getName().equals(name))
                .toList();
    }

    @Override
    public Optional<Waiting> findFirstWaitingByScheduleForUpdate(Schedule schedule) {
        return waitingList.stream()
                .filter(r -> r.getSchedule().equals(schedule))
                .findFirst();
    }

    @Override
    public Long findWaitingOrder(Waiting waiting) {
        return waitingList.stream()
                .filter(w -> w.getSchedule().equals(waiting.getSchedule()))
                .filter(w -> w.getId() < waiting.getId())
                .count() + 1;
    }

    @Override
    public void delete(Waiting waiting) {
        waitingList.removeIf(w -> w.getId().equals(waiting.getId()));
    }

    public List<Waiting> findAll() {
        return List.copyOf(waitingList);
    }
}
