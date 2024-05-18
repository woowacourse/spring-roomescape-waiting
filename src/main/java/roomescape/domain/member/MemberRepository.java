package roomescape.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(MemberEmail email);

    boolean existsByEmail(MemberEmail email);
}
