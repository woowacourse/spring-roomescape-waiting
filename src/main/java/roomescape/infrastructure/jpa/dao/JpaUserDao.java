package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;

import java.util.Optional;

public interface JpaUserDao extends JpaRepository<User, Id> {

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
