package roomescape.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);
}
