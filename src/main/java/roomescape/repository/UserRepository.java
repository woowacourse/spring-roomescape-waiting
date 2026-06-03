package roomescape.repository;

import java.util.Optional;
import roomescape.domain.User;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Long save(User user);

    boolean existsByUsername(String username);
}
