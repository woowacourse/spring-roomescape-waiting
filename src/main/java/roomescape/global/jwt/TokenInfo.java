package roomescape.global.jwt;

import roomescape.member.domain.Role;

public class TokenInfo {
    private Long id;
    private Role role;

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
