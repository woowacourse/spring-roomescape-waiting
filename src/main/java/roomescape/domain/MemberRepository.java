package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    boolean existByEmail(final String email);

    boolean existByName(final String name);

    Member save(final Member member);

    Optional<Member> findByEmail(final String email);

    Optional<Member> findById(final long id);

    List<Member> findAll();
}
