package roomescape.member.domain.repository;

import java.util.Collection;
import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepository {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    Collection<Member> findAll();

    Member save(Member member);

    Optional<Member> findById(Long id);
}
