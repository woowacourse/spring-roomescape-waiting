package roomescape.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.user.Email;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.NotFoundException;

public interface UserJpaRepository extends UserRepository, Repository<User, Long> {

    @Override
    default User getById(final Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + id));
    }

    @Override
    Optional<User> findByEmail(final Email email);

    @Modifying
    @Query("DELETE FROM USERS u WHERE u.id = :id")
    @Transactional
    int deleteByIdAndCount(final Long id);

    @Transactional
    default void deleteByIdOrElseThrow(final Long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 사용자입니다. id : " + id);
        }
    }
}
