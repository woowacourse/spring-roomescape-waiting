package roomescape.infrastructure.Jpa;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JpaUserRepository implements UserRepository {

    private final JpaUserDao dao;

    public JpaUserRepository(JpaUserDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(User user) {
        dao.save(user);
    }

    @Override
    public List<User> findAll() {
        return dao.findAll();
    }

    @Override
    public Optional<User> findById(Id userId) {
        return dao.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return dao.findByEmail(new Email(email));
    }

    @Override
    public boolean existByEmail(String email) {
        return dao.existsByEmail(new Email(email));
    }
}
