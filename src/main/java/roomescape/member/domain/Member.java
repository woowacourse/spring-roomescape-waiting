package roomescape.member.domain;

import java.util.Objects;

public class Member {

    private static final long EMPTY_ID = 0L;

    private final Long id;
    private final MemberName name;
    private final Email email;
    private final Password password;
    private final Role role;

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        validateNull(id, name, email, password);
        this.id = id;
        this.name = new MemberName(name);
        this.email = new Email(email);
        this.password = new Password(password);
        this.role = role;
    }

    public Member(final String name, final String email, final String password, final Role role) {
        this(EMPTY_ID, name, email, password, role);
    }

    private void validateNull(final Long id, final String name, final String email, final String password) {
        if (id == null || name == null || email == null || password == null) {
            throw new IllegalArgumentException("사용자 정보가 존재하지 않습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.value();
    }

    public String getEmail() {
        return email.value();
    }

    public String getPassword() {
        return password.value();
    }

    public String getRole() {
        return role.name();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
