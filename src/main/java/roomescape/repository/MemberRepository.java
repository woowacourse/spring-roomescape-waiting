package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
