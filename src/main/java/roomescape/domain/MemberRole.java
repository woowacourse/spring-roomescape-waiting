package roomescape.domain;

import java.util.Arrays;
import java.util.Objects;

public enum MemberRole {

    USER("USER"),
    ADMIN("ADMIN"),
    NONE("NONE");

    private final String name;

    MemberRole(final String name) {
        this.name = name;
    }

    public static MemberRole fromName(final String name) {
        return Arrays.stream(MemberRole.values())
                .filter(role -> Objects.equals(role.name, name))
                .findAny()
                .orElse(MemberRole.NONE);
    }

    public boolean isAdmin() {
        return this == MemberRole.ADMIN;
    }

    public String getName() {

        return name;
    }
}
