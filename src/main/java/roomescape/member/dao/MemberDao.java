package roomescape.member.dao;

import java.util.Optional;
import roomescape.common.Dao;
import roomescape.member.domain.Member;

public interface MemberDao extends Dao<Member> {
    Optional<Member> findByEmail(String email);

    Boolean existsByEmail(String email);
}
