package roomescape.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);
}
