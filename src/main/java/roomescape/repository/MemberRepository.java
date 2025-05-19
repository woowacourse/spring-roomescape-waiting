package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;

public interface MemberRepository extends ListCrudRepository<Member, Long> {

    Optional<Member> findByEmail(final MemberEmail email);
}
