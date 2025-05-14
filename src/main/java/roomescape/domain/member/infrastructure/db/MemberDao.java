package roomescape.domain.member.infrastructure.db;

import java.util.List;
import java.util.Optional;
import roomescape.domain.member.model.Member;

public interface MemberDao {

    Optional<Member> selectByEmailAndPassword(String email, String password);

    List<Member> getAll();

    boolean existsById(Long memberId);
}
