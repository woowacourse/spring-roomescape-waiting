package roomescape.service.fake;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Waiting save(Waiting waiting) {
        Waiting saved = new Waiting(
                sequence.incrementAndGet(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme());
        waitings.add(saved);
        return saved;
    }

    @Override
    public Optional<Waiting> findByScheduleAndName(Waiting waiting) {
        return waitings.stream()
                .filter(w -> isSameSchedule(w, waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId()))
                .filter(w -> w.getName().equals(waiting.getName()))
                .findFirst();
    }

    @Override
    public Optional<Waiting> findById(long id) {
        return waitings.stream()
                .filter(w -> w.getId().equals(id))
                .findFirst();
    }

    @Override
    public Long findWaitingOrder(Long id, Theme theme, LocalDate date, ReservationTime time) {
        return waitings.stream()
                .filter(w -> isSameSchedule(w, date, time.getId(), theme.getId()))
                .filter(w -> w.getId() < id)
                .count() + 1;
    }

    @Override
    public void delete(Waiting waiting) {
        waitings.removeIf(w -> w.getId().equals(waiting.getId()));
    }

    public List<Waiting> findAll() {
        return List.copyOf(waitings);
    }

    private boolean isSameSchedule(Waiting waiting, LocalDate date, Long timeId, Long themeId) {
        return waiting.getDate().equals(date)
                && waiting.getTime().getId().equals(timeId)
                && waiting.getTheme().getId().equals(themeId);
    }
}
