package roomescape.domain.member;

import java.util.Objects;

public class Member {

    private final Long id;
    private final String name;
    private final Email email;
    private final String password;
    private final Role role;

    public Member(String name, Email email, String password, Role role) {
        this(null, name, email, password, role);
    }

    public Member(Long id, String name, Email email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isNotPassword(String password) {
        return !this.password.equals(password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id)
                && Objects.equals(name, member.name)
                && Objects.equals(email, member.email)
                && Objects.equals(password, member.password)
                && role == member.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role);
    }
}
