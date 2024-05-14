package roomescape.member.domain;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existByEmail(String email);

    boolean existByName(String name);
}
