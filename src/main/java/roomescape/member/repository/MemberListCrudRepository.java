package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public interface MemberListCrudRepository extends ListCrudRepository<Member, Long> {

    boolean existsByEmail(Email email);

    Optional<Member> findByEmailAndPassword(Email email, Password password);
}
