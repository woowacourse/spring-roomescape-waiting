package roomescape.domain;

import roomescape.auth.Role;
import roomescape.exception.LoginFailedException;

public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member withId(Long id) {
        return new Member(id, name, email, password, role);
    }

    public void validatePassword(String password) {
        if (!this.password.equals(password)) {
            throw new LoginFailedException();
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
