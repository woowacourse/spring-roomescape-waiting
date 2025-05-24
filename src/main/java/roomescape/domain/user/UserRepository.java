package roomescape.domain.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.BaseRepository;
import roomescape.exception.NotFoundException;

public interface UserRepository extends BaseRepository<User, Long> {

    @Override
    User save(User user);

    @Override
    Optional<User> findById(Long id);

    Optional<User> findByEmail(Email email);

    @Override
    User getById(Long id) throws NotFoundException;

    List<User> findAll();

    @Override
    List<User> findAll(Specification<User> specification);

    @Override
    boolean exists(Specification<User> specification);

    @Override
    void delete(User entity);

    @Override
    void deleteByIdOrElseThrow(Long id) throws NotFoundException;
}
