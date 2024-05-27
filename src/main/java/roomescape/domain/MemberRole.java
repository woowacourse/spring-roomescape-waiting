package roomescape.domain;

import java.util.Arrays;

public enum MemberRole {

    ADMIN,
    USER;

    public static MemberRole from(String value) {
        return Arrays.stream(values())
                .filter(memberRole -> memberRole.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 역할이 존재하지 않습니다."));
    }

    public boolean isAdmin() {
        return ADMIN.equals(this);
    }
}
