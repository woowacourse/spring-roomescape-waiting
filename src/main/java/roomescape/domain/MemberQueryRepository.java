package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface MemberQueryRepository {

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    List<Member> findAll();
}
