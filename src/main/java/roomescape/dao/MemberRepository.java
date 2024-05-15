package roomescape.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.user.Member;

public interface MemberRepository extends JpaRepository<Member,Long> {
}
