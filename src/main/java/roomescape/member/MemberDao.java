package roomescape.member;

import java.util.Optional;

public interface MemberDao {
    Member insert(Member member);
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
}
