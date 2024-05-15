package roomescape.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.user.Email;
import roomescape.domain.user.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
