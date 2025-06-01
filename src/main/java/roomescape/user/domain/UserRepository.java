package roomescape.user.domain;

import roomescape.common.domain.Email;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(Email email);

    List<User> findAll();

    List<User> findAllByIds(List<Long> ids);

    User save(User user);
}
