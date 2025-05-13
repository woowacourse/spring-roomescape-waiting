package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberDao {

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(long memberId);

    Member save(Member member);

    List<Member> findAll();
}
