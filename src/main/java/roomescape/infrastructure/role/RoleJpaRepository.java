package roomescape.infrastructure.role;

import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.RoleRepository;

public interface RoleJpaRepository extends RoleRepository, ListCrudRepository<MemberRole, Long> {

    @Override
    @Query("""
            select case when exists (
                select 1 from MemberRole as mr
                where mr.id = :memberId and mr.role != roomescape.domain.role.Role.ADMIN
            ) then true else false end
            """)
    boolean isNotAdminByMemberId(long memberId);

    @Override
    default MemberRole getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("역할을 찾을 수 없습니다."));
    }
}
