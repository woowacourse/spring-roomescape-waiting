package roomescape.domain.member;

import java.util.List;

public enum MemberRole {

    USER(List.of("ROLE_USER")),
    ADMIN(List.of("ROLE_ADMIN", "ROLE_USER")),
    NONE(List.of("ROLE_NONE"));

    public final List<String> name;

    MemberRole(final List<String> name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return this.name.contains("ROLE_ADMIN");
    }
}
