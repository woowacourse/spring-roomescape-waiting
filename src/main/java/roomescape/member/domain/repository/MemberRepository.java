package roomescape.member.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

//    Optional<Member> findByEmail(String email);
//
//    Boolean existsByEmail(String email);
}
