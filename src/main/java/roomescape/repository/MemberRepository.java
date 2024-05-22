package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findByEmail(String email);

    default Member getMemberById(long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 입니다"));
    }
}
