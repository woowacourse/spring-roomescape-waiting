package roomescape.domain.role;

public interface RoleRepository {

    MemberRole save(MemberRole memberRole);

    boolean isAdminByMemberId(long memberId);

    MemberRole getById(long id);
}
