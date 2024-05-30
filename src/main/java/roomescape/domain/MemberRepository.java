package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.service.exception.ResourceNotFoundCustomException;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(String email, String password);

    default Member getMemberById(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundCustomException("아이디에 해당하는 사용자를 찾을 수 없습니다."));
    }
}
