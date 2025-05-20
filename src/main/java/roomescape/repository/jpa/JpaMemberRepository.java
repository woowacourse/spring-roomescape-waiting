package roomescape.repository.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
