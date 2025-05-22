package roomescape.user.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.domain.Email;
import roomescape.user.domain.User;
import roomescape.user.domain.UserRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findById(final Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(final Email email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public List<User> findAllByIds(final List<Long> ids) {
        return jpaUserRepository.findAllByIdIn(ids);
    }

    @Override
    public User save(final User user) {
        return jpaUserRepository.save(user);
    }
}
