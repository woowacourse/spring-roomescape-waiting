package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.service.exception.MemberNotFoundException;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    default Member findByEmailOrThrow(String email) {
        return findByEmail(email).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 멤버입니다."));
    }

    default Member findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 멤버입니다."));
    }
}
