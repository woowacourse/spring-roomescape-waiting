package roomescape.member;

import java.util.Objects;

public class Member {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final MemberRole role;

    public Member(final Long id, final String email, final String password, final String name, final MemberRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Member(final String email, final String password, final String name, final MemberRole role) {
        this(null, email, password, name, role);
    }

    public boolean matchesPassword(final String password) {
        return Objects.equals(this.password, password);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public MemberRole getRole() {
        return role;
    }
}
