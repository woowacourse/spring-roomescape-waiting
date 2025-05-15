package roomescape.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
