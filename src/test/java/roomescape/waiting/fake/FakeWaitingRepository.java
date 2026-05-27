package roomescape.waiting.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> store = new HashMap<>();
    private Long sequence = 1L;

    @Override
    public Waiting save(Waiting waiting) {
        if (waiting.getId() == null) {
            Waiting saved = Waiting.createRow(
                    sequence++,
                    waiting.getName(),
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme(),
                    waiting.getRank(),
                    waiting.getCreatedAt()
            );
            store.put(saved.getId(), saved);
            return saved;
        }

        store.put(waiting.getId(), waiting);
        return waiting;
    }

    public Optional<Waiting> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Waiting> findByName(String name) {
        return store.values().stream()
                .filter(waiting -> waiting.getName().equals(name))
                .toList();
    }

    @Override
    public void deleteByIdAndName(Long id, String name) {
        store.values().removeIf(waiting ->
                waiting.getId().equals(id)
                        && waiting.getName().equals(name)
        );
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }
}
