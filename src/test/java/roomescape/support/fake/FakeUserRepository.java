package roomescape.support.fake;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;

public class FakeUserRepository implements UserRepository {

    private final Map<Long, User> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public User save(User user) {
        Long id = user.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        User savedUser = User.createWithId(id, user);
        storage.put(id, savedUser);
        return savedUser;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<User> findByName(String name) {
        return storage.values().stream()
            .filter(user -> name.equals(user.getName()))
            .findFirst();
    }
}
