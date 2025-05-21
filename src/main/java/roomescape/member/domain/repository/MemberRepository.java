package roomescape.member.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface MemberRepository {

    List<Member> findByMemberRole(MemberRole memberRole);

    boolean existsByEmail(String email);

    Member save(Member member);

    Optional<Member> findById(Long id);
}
