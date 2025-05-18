package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

import java.util.Optional;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}
