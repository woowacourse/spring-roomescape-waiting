package roomescape.domain;

import java.util.Objects;

import lombok.Getter;

@Getter
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

    public Member(Long id, String name, String email, String password) {
        this(id, name, email, password, Role.NORMAL);
    }

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password, Role.NORMAL);
    }

    public String getRoleAsString() {
        return role.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member user = (Member) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
