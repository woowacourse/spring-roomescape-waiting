package roomescape.member.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndPassword(String email, String password);
}
