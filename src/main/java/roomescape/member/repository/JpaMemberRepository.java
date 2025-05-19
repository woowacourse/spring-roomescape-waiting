package roomescape.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(MemberEmail email);
}
