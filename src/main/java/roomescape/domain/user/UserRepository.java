package roomescape.domain.user;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByName(String name);

    User save(User user);

    boolean existsByName(String name);
}
