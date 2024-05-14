package roomescape.core.repository;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.core.domain.Member;

public interface MemberRepository extends ListCrudRepository<Member, Long> {
    Member findByEmailAndPassword(final String email, final String password);

    Member findByEmail(final String email);
}
