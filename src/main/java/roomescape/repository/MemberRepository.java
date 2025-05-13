package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {

    boolean existBySessionId(final String sessionId);

    boolean existByEmail(final String email);

    boolean existByName(final String name);

    Member save(final Member member);

    Optional<Member> findByEmail(final String email);

    void updateSessionId(final long memberId, final String sessionId);

    Optional<Member> findById(final long id);

    List<Member> findAll();
}
