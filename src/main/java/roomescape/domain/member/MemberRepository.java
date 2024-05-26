package roomescape.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.exception.InvalidMemberException;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(Email email);

    default Member getByEmail(String email) {
        return findByEmail(Email.of(email))
                .orElseThrow(() -> new InvalidMemberException("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    default Member getById(long memberId) {
        return findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("회원 정보를 찾을 수 없습니다."));
    }

    boolean existsByEmail(Email email);
}
