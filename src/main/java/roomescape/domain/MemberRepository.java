package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    Member save(Member member);
}
