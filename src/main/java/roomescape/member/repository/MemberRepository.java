package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    List<Member> findAll();
}
