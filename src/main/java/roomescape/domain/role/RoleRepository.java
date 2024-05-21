package roomescape.domain.role;

public interface RoleRepository {

    MemberRole save(MemberRole memberRole);

    boolean isNotAdminByMemberId(long memberId);

    MemberRole getById(long id);
}
