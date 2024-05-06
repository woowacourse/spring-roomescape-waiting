package roomescape.domain.role;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface RoleRepository {

    MemberRole save(MemberRole memberRole);

    boolean isAdminByMemberId(long memberId);

    Optional<MemberRole> findByMemberId(long id);

    default MemberRole getByMemberId(long id) {
        return findByMemberId(id).orElseThrow(() -> new NoSuchElementException("역할을 찾을 수 없습니다."));
    }
}
