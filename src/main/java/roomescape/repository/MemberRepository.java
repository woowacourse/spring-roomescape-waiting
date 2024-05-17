package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberPassword;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(MemberEmail email, MemberPassword password);
}
