package roomescape.member.domain;

public class Member {
    private final Long id;
    private final MemberName memberName;
    private final Email email;
    private final Password password;
    private final Role role;

    public Member(
            final Long id, final MemberName memberName, final Email email,
            final Password password, final Role role
    ) {
        this.id = id;
        this.memberName = memberName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member of(
            final long id, final String name, final String email,
            final String role, final String password
    ) {
        return new Member(id, new MemberName(name), new Email(email), new Password(password), Role.from(role));
    }

    public static Member of(final String name, final String email, final String password, final String role) {
        return new Member(null, new MemberName(name), new Email(email), new Password(password), Role.from(role));
    }

    public Long getId() {
        return id;
    }

    public MemberName getMemberName() {
        return memberName;
    }

    public String getNameValue() {
        return memberName.getValue();
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
}
