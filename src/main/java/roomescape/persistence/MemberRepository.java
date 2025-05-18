package roomescape.persistence;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.Member;

public interface MemberRepository extends ListCrudRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
