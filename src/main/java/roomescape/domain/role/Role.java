package roomescape.domain.role;

import java.util.Arrays;

public enum Role {
    ADMIN("admin"),
    MEMBER("member"),
    ;

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public static Role from(String roleName) {
        return Arrays.stream(Role.values())
                .filter(role -> role.hasRoleNameOf(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당하는 역할이 없습니다."));
    }

    private boolean hasRoleNameOf(String otherName) {
        return roleName.equalsIgnoreCase(otherName);
    }
}
