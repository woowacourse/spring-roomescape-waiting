package roomescape.member.repository;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);

    default Member getByEmail(Email email) {
        return findByEmail(email).orElseThrow(
                () -> new NoSuchElementException("이메일 " + email.getEmail() + "에 해당하는 회원이 존재하지 않습니다."));
    }

    default Member getById(Long id) {
        return findById(id).orElseThrow(
                () -> new NoSuchElementException("식별자 " + id + "에 해당하는 회원이 존재하지 않습니다."));
    }
}
