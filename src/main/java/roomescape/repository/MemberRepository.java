package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.service.exception.MemberNotFoundException;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    default Member fetchByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 멤버입니다."));
    }

    default Member fetchById(long id) {
        return findById(id).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 멤버입니다."));
    }
}
