package roomescape.domain.user;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exception.NotFoundException;

public interface UserRepository extends ListCrudRepository<User, Long> {

    Optional<User> findByEmail(Email email);

    default User getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + id));
    }
}
