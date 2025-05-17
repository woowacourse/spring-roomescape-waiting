package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

public interface JpaMemberRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);

    boolean existsByEmail(Email email);

    List<Member> findAll();
}
