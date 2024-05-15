package roomescape.domain.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.domain.Member;


public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);
}
