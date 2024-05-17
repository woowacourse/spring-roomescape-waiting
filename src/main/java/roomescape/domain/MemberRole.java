package roomescape.domain;

import java.util.Arrays;

public enum MemberRole {

    ADMIN,
    USER,
    ;

    public static MemberRole from(String role) {
        return Arrays.stream(values())
            .filter(memberRole -> memberRole.name().equals(role))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("올바른 Role을 입력해주세요."));
    }
}
