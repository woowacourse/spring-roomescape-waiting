package roomescape.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.exception.DomainNotFoundException;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    default Member getByIdentifier(long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException("해당 id의 회원이 존재하지 않습니다."));
    }

    default Member getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new DomainNotFoundException("해당 이메일의 회원이 존재하지 않습니다."));
    }
}
