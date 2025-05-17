package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberQueryRepository {

    Member getByIdOrThrow(Long id);

    Optional<Member> findByEmail(String email);

    List<Member> findAll();
}
