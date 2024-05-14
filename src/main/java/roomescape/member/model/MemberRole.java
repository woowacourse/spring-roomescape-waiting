package roomescape.member.model;

import java.util.Arrays;

public enum MemberRole {
    ADMIN, USER;

    public static MemberRole of(final String value) {
        return Arrays.stream(MemberRole.values())
                .filter(role -> role.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회원 권한 값입니다."));
    }
}
