package roomescape.domain;

import java.util.Arrays;

public enum MemberRole {
    USER(1),
    ADMIN(2);

    private final int level;

    MemberRole(int level) {
        this.level = level;
    }

    public static MemberRole findByName(String role) {
        return Arrays.stream(MemberRole.values())
                .filter(r -> r.name().equals(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다."));
    }

    public boolean isLowerThan(MemberRole other) {
        return this.level < other.level;
    }
}
