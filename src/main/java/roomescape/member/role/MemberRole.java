package roomescape.member.role;

import java.util.Arrays;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

public enum MemberRole {

    NON_MEMBER("nonMember"),
    MEMBER("member"),
    ADMIN("admin");

    private final String roleName;

    MemberRole(String roleName) {
        this.roleName = roleName;
    }

    public static MemberRole findMemberRole(String role) {
        return Arrays.stream(MemberRole.values())
                .filter(memberRole -> memberRole.roleName.equals(role))
                .findAny()
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_ROLE_NOT_EXIST_EXCEPTION));
    }

    public boolean hasSameRoleFrom(MemberRole[] roles) {
        for (MemberRole memberRole : roles) {
            if (this.equals(memberRole)) {
                return true;
            }
        }
        return false;
    }
}
