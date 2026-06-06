package roomescape.member.repository;

import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
