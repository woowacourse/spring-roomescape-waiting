package roomescape.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberId;
import roomescape.member.domain.Password;

public interface MemberRepository extends JpaRepository<Member, MemberId> {

    Optional<Member> findByEmailAndPassword(Email email, Password password);

    default Optional<Member> findByEmailAndPassword(String email, String password) {
        return findByEmailAndPassword(new Email(email), new Password(password));
    }
}
