package roomescape.dao;

import java.util.List;
import java.util.Optional;
import roomescape.model.Member;

public interface MemberDao {
    Optional<Member> findByEmail(final String email);

    Optional<Member> findById(final Long id);

    List<Member> findAll();
}
