package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.member.UnauthorizedEmailException;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    default Member fetchByEmail(String email) {
        return findByEmail(email).orElseThrow(UnauthorizedEmailException::new);
    }

    default Member fetchById(long memberId) {
        return findById(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
