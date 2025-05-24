package roomescape.business.model.repository;

import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;

import java.util.List;
import java.util.Optional;

public interface Users {

    void save(User user);

    List<User> findAll();

    Optional<User> findById(Id userId);

    Optional<User> findByEmail(String email);

    boolean existByEmail(String email);
}
