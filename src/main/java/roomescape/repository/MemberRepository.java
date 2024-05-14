package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    default Member fetchByEmail(String email) {
        return findByEmail(email).orElseThrow();
    }

    default Member fetchById(long id) {
        return findById(id).orElseThrow();
    }
}
