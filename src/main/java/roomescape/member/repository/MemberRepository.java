package roomescape.member.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(final String email, final Password password);

    boolean existsByEmail(final String email);
}
