package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.user.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAddress(String email);

    boolean existsByEmailAddress(String email);
}
