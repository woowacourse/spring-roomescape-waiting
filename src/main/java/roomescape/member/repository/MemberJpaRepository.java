package roomescape.member.repository;

import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

import java.util.Optional;

public interface MemberJpaRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);
}
