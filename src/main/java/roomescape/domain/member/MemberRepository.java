package roomescape.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.exception.InvalidMemberException;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(Email email);

    default Member getByEmail(Email email) {
        return findByEmail(email).orElseThrow(() -> new InvalidMemberException("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    default Member getById(long id) {
        return findById(id).orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }

    boolean existsByEmail(Email email);
}
