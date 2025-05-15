package roomescape.member.infrastructure.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.model.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);
}
