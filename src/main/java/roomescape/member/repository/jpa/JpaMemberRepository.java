package roomescape.member.repository.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByName(String name);
}
