package roomescape.member.business.domain;

import java.util.Objects;

public class Member {

    private final Long id;
    private final MemberName name;
    private final Email email;
    private final Password password;
    private final Role role;

    public Member(final Long id, final MemberName name, final Email email, final Password password, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        this(id, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final String name, final String email, final String password, final Role role) {
        this(null, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final String name, final String email, final String password) {
        this(null, new MemberName(name), new Email(email), new Password(password), Role.MEMBER);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
