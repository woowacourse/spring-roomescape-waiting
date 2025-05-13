package roomescape.infrastructure.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.User;
import roomescape.domain.repository.UserRepository;

public class UserFakeRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Optional<User> findById(final long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public long save(final User user) {
        var id = index.getAndIncrement();
        users.put(id, user);
        return id;
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return users.values()
            .stream()
            .filter(user -> user.email().equals(email))
            .findAny();
    }
}
