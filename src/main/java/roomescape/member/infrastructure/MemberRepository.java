package roomescape.member.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getByIdOrThrow(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));
    }

    Optional<Member> findByEmail(String email);
}
