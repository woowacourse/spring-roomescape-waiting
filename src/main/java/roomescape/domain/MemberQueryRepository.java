package roomescape.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberQueryRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(Email email);
}
