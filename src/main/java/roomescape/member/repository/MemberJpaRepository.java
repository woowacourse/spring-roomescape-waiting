package roomescape.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long>, MemberRepository {
}
