package roomescape.user.repository;

import java.util.List;
import java.util.Optional;
import roomescape.user.domain.User;

public interface UserRepository {

    Optional<User> findUserByEmailAndPassword(String email, String password);

    User save(User user);

    List<User> findAll();

    User findByIdOrThrow(Long id);

    Optional<User> findById(Long id);

    boolean existUserByEmailAndPassword(String email, String password);

    Optional<User> findUseByEmail(String email);
}
