package roomescape.business.model.repository;

import java.util.List;
import java.util.Optional;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;

public interface UserRepository {

    void save(User user);

    List<User> findAll();

    Optional<User> findById(Id userId);

    Optional<User> findByEmail(String email);

    boolean existByEmail(String email);
}
