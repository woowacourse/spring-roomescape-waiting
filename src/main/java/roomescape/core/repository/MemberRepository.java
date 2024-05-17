package roomescape.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.core.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmailAndPassword(final String email, final String password);

    Member findByEmail(final String email);
}
