package roomescape.auth.domain;

import lombok.Getter;

@Getter
public enum AuthRole {

    ADMIN("어드민"),
    MEMBER("회원"),
    GUEST("게스트"),
    ;

    private final String roleName;

    AuthRole(final String roleName) {
        this.roleName = roleName;
    }
}
