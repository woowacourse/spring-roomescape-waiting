package roomescape.domain.user;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByName(String name);

    boolean existsByName(String name);

    User save(User user);
}
