package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(final MemberEmail email);
}
