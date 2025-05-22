package roomescape.domain.member;

import java.util.Arrays;
import java.util.List;

public enum MemberRole {
    ADMIN(List.of("ADMIN")),
    MEMBER(List.of("MEMBER")),
    ;

    List<String> types;

    MemberRole(final List<String> types) {
        this.types = types;
    }

    public static MemberRole from(final String roleName) {
        return Arrays.stream(values())
                .filter(role -> role.types.contains(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 권한입니다."));
    }

    public String getPrimaryType() {
        return name();
    }
}
