package roomescape.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MemberQueryRepository extends Repository<Member, Long> {

    Optional<Member> findById(Long id);

    List<Member> findAll();

    Optional<Member> findByEmail(Email email);
}
