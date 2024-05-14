package roomescape.auth.domain;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class AuthInfo {
    private final Long id;
    private final String name;
    private final String email;
    private final Role role;

    public AuthInfo(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static AuthInfo of(Member member) {
        return new AuthInfo(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
