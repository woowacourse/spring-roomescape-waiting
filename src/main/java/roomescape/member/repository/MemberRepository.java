package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findFirstByEmailAndPassword(String email, String password);

    Optional<Member> findFirstByEmail(String email);
}
