package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface JpaMemberRepository extends ListCrudRepository<Member, Long> {

    List<Member> findByMemberRole(final MemberRole memberRole);

    Optional<Member> findByEmail(final String email);

    boolean existsByEmail(String email);
}
