package roomescape.repository;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.Member;

public interface MemberListCrudRepository extends ListCrudRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmailAndPassword(String email, String password);
}
