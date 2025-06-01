package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface MemberRepository extends ListCrudRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    List<Member> findByMemberRole(MemberRole memberRole);

    boolean existsByEmail(String email);
}
