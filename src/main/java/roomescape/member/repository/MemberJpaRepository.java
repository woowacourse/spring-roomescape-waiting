package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

public interface MemberJpaRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);
}
