package roomescape.domain;

import java.util.Arrays;

public enum MemberRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String name;

    MemberRole(String name) {
        this.name = name;
    }

    public static MemberRole of(String role) {
        return Arrays.stream(MemberRole.values())
                .filter(r -> r.name.equals(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 권한입니다."));
    }
}
