package roomescape.member.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndPassword(Email email, Password password);

    Optional<Member> findByEmail(Email email);
}
