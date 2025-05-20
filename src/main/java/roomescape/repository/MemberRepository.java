package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {

    boolean existByEmail(final String email);

    boolean existByName(final String name);

    Member save(final Member member);

    Optional<Member> findByEmail(final String email);

    Optional<Member> findById(final long id);

    List<Member> findAll();
}
