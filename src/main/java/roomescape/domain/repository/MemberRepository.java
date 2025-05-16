package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    Member save(Member member);

    List<Member> findAll();
}
