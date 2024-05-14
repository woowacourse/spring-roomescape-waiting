package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public interface MemberRepository extends ListCrudRepository<Member, Long> {

    Optional<Member> findByEmailValueAndPasswordValue(String email, String password);
}
