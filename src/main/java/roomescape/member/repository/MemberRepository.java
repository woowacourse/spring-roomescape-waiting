package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

@Repository
public interface MemberRepository extends ListCrudRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(Email email, Password password);
}
