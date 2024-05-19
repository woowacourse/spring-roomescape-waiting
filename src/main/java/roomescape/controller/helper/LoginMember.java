package roomescape.controller.helper;

import roomescape.domain.member.Role;

public class LoginMember {

    private final String email;
    private final String name;
    private final Role role;

    public LoginMember(String email, String name, Role role) {
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
