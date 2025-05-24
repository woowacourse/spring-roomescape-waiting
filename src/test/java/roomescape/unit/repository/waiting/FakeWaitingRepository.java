package roomescape.unit.repository.waiting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.repository.waiting.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Waiting> waitings = new ArrayList<>();

    @Override
    public long save(Waiting waiting) {
        long id = index.getAndIncrement();
        Waiting saved = new Waiting(id, waiting.getDate(), waiting.getTime(), waiting.getTheme(), waiting.getMember());
        waitings.add(saved);
        return id;
    }

    @Override
    public List<Waiting> findAll() {
        return Collections.unmodifiableList(waitings);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitings.stream()
                .filter(waiting -> waiting.getId().equals(id))
                .findAny();
    }

    @Override
    public List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return waitings.stream()
                .filter(waiting -> waiting.getMember().getId().equals(memberId))
                .map(waiting -> {
                    long rank = waitings.stream()
                            .filter(waiting2 -> waiting2.getTheme().equals(waiting.getTheme())
                                    && waiting2.getDate().equals(waiting.getDate())
                                    && waiting2.getTime().equals(waiting.getTime())
                                    && waiting2.getId() < waiting.getId())
                            .count() + 1;
                    return new WaitingWithRank(waiting, rank);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndMember(Waiting waiting) {
        return waitings.stream().anyMatch(w ->
                w.getDate().equals(waiting.getDate())
                        && w.getTime().equals(waiting.getTime())
                        && w.getTheme().getId().equals(waiting.getTheme().getId())
                        && w.getMember().getId().equals(waiting.getMember().getId())
        );
    }

    @Override
    public void deleteById(Long id) {
        waitings.removeIf(waiting -> waiting.getId().equals(id));
    }
}
