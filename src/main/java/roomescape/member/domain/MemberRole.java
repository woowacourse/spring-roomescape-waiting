package roomescape.member.domain;

import java.util.Arrays;
import java.util.Objects;

public enum MemberRole {
    ADMIN,
    USER,
    ;

    public static MemberRole getMemberRole(final String role) {
        return Arrays.stream(values())
                .filter(memberRole -> Objects.equals(memberRole.name(), role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 권한입니다. 다시 조회해주세요."));
    }

    public boolean isNotAdmin() {
        return this != ADMIN;
    }
}
