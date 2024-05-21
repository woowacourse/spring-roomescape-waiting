package roomescape.domain.member;

import java.util.Arrays;
import roomescape.exception.member.InvalidMemberRoleException;

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
                .orElseThrow(InvalidMemberRoleException::new);
    }

    public boolean isLowerThan(MemberRole other) {
        return this.level < other.level;
    }
}
