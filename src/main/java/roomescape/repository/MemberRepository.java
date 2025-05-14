package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.entity.Member;

public interface MemberRepository {

    List<Member> findAll();

    Member findByEmail(String email);

    Optional<Member> findById(Long id);

    boolean existsByEmail(String email);

    Member save(Member member);
}
