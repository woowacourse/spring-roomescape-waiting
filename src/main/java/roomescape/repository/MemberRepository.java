package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.user.Member;
import roomescape.exception.NotExistException;

import java.util.Optional;

import static roomescape.exception.ExceptionDomainType.MEMBER;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAddress(String email);

    boolean existsByEmailAddress(String email);

    default Member getById(final Long id) {
        return findById(id).orElseThrow(() -> {
            throw new NotExistException(MEMBER, id);
        });
    }
}
