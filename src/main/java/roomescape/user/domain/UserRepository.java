package roomescape.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByName(String name);
}
