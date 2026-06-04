package roomescape.domain.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByName(String name);

    User save(User user);

    default User findByNameOrThrow(String name) {
        return findByName(name)
                .orElseThrow();
    }
}
