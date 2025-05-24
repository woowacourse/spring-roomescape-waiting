package roomescape.global.jwt;

import roomescape.member.domain.Role;

public class TokenInfo {

    private final Long id;
    private final Role role;

    public TokenInfo(Long id, Role role) {
        this.id = id;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }
}
