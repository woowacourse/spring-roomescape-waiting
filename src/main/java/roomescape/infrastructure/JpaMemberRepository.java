package roomescape.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
