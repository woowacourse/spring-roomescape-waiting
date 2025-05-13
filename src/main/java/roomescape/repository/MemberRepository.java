package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;

@Repository
public interface MemberRepository {
    Member add(Member member);

    List<Member> findAll();

    Optional<Member> findById(long id);

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
