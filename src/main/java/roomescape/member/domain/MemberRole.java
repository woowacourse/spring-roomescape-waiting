package roomescape.member.domain;

import java.util.Arrays;

public enum MemberRole {

    ADMIN, USER;

    public static MemberRole from(final String input) {
        return Arrays.stream(MemberRole.values())
                .filter(memberRole -> memberRole.name().equals(input))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 멤버 role입니다."));
    }
}
