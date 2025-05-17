package roomescape.member.repository.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);
}
