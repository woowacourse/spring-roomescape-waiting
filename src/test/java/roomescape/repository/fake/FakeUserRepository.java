package roomescape.repository.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.User;
import roomescape.repository.UserRepository;

public class FakeUserRepository implements UserRepository {

    private final Map<Long, User> store = new HashMap<>();
    private long nextId = 1L;

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Long save(User user) {
        Long id = nextId++;
        User saved = user.withId(id);
        store.put(id, saved);
        return id;
    }

    @Override
    public boolean existsByUsername(String username) {
        return store.values().stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }
}