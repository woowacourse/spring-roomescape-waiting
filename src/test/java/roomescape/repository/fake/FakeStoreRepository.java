package roomescape.repository.fake;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import roomescape.domain.Store;
import roomescape.repository.StoreRepository;

public class FakeStoreRepository implements StoreRepository {

    private final Map<Long, Store> store = new HashMap<>();
    private final Map<Long, Set<Long>> storeIdsByUserId = new HashMap<>();
    private long nextId = 1L;

    public long save(Store value) {
        long id = nextId++;
        store.put(id, value.withId(id));
        return id;
    }

    public void assignManager(Long storeId, Long userId) {
        storeIdsByUserId.computeIfAbsent(userId, key -> new HashSet<>()).add(storeId);
    }

    @Override
    public Optional<Store> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsByStoreIdAndUserId(Long storeId, Long userId) {
        return storeIdsByUserId.getOrDefault(userId, Set.of()).contains(storeId);
    }

    @Override
    public List<Long> findStoreIdsByUserId(Long userId) {
        return List.copyOf(storeIdsByUserId.getOrDefault(userId, Set.of()));
    }
}