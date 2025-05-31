package roomescape.infrastructure.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}
