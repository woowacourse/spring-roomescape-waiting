package roomescape.auth.domain;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum AuthRole {

    ADMIN("어드민"),
    MEMBER("회원"),
    GUEST("게스트"),
    ;

    private final String roleName;

    AuthRole(String roleName) {
        this.roleName = roleName;
    }

    public static AuthRole from(final String roleName) {
        return Arrays.stream(values())
                .filter(authRole -> authRole.roleName.equals(roleName))
                .findFirst()
                .orElse(AuthRole.GUEST);
    }
}
