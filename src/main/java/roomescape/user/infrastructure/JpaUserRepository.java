package roomescape.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.common.domain.Email;
import roomescape.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(Email email);

    List<User> findAllByIdIn(List<Long> ids);
}
