package roomescape.repository;

import jakarta.persistence.Id;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Members;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
