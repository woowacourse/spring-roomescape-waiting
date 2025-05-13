package roomescape.domain;

import java.util.Objects;

public class Member {

    private final Long id;
    private final String name;
    private final Role role;
    private final String email;
    private final String password;

    private Member(Long id, String name, Role role, String email, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public static Member create(String name, Role role, String email, String password) {
        return new Member(null, name, role, email, password);
    }

    public static Member create(Long id, String name, Role role, String email, String password) {
        return new Member(id, name, role, email, password);
    }

    public boolean isIncorrectPassword(String password) {
        return !this.password.equals(password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
