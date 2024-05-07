package roomescape.domain;

import java.util.Objects;

public class Member {

    private final Long id;
    private final MemberName name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(Long id, MemberName name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(Long id, String name, String email, String password, Role role) {
        this(id, new MemberName(name), email, password, role);
    }

    public Member(String name, String email, String password, Role role) {
        this(null, new MemberName(name), email, password, role);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getEmail() {
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
