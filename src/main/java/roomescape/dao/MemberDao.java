package roomescape.dao;

import java.util.Optional;
import roomescape.domain.member.Member;

public interface MemberDao {
    Member insert(Member member);
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
}
