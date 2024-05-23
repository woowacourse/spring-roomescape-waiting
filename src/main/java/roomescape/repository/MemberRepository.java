package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findMemberByEmailAndPassword(Email email, Password password);
}
