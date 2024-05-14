package roomescape.domain.role;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface RoleRepository extends ListCrudRepository<MemberRole, Long> {

    @Query("""
            select case when exists (
                select 1 from MemberRole as mr
                where mr.memberId = :memberId and mr.role = roomescape.domain.role.Role.ADMIN
            ) then true else false end
            """)
    boolean isAdminByMemberId(long memberId);

    Optional<MemberRole> findByMemberId(long id);

    default MemberRole getByMemberId(long id) {
        return findByMemberId(id).orElseThrow(() -> new NoSuchElementException("역할을 찾을 수 없습니다."));
    }
}
