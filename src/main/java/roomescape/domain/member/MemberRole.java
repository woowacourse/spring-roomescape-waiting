package roomescape.domain.member;

import java.util.Arrays;

public enum MemberRole {
    ADMIN,
    MEMBER,
    ;

    public static MemberRole from(final String roleName) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 권한입니다."));
    }
}
