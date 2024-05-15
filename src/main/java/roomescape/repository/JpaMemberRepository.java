package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.service.exception.UnauthorizedEmailException;
import roomescape.service.exception.MemberNotFoundException;

import java.util.Optional;

@Repository
public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    default Member fetchByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> new UnauthorizedEmailException("이메일이 존재하지 않습니다."));
    }

    default Member fetchById(long memberId) {
        return findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }
}
