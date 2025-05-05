package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberQueryRepository {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    Member getById(Long id);

    List<Member> findAll();
}
