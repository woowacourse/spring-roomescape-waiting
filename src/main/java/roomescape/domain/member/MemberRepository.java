package roomescape.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.RoomEscapeException;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 회원을 찾을 수 없습니다. : " + id));
    }
}
