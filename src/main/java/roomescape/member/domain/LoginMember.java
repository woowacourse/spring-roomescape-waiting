package roomescape.member.domain;

import java.util.Objects;

public class LoginMember {

    private final Long id;
    private final Name name;
    private final Email email;
    private final Role role;

    public LoginMember(Long id, Name name, Email email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public LoginMember(Name name, Email email, Role role) {
        this(null, name, email, role);
    }

    public LoginMember(Long id, String name, String email, String role) {
        this(id, new Name(name), new Email(email), Role.getByDbValue(role));
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoginMember that = (LoginMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
