package roomescape.member.model;

public class Member {

    private final Long id;
    private final MemberRole role;

    private final MemberEmail email;
    private final MemberName name;
    private final MemberPassword password;

    public static Member createMemberWithoutId(
            final MemberRole role,
            final String password,
            final String name,
            final String email
    ) {
        return new Member(
                null,
                role,
                new MemberPassword(password),
                new MemberName(name),
                new MemberEmail(email)
        );
    }

    public static Member createMemberWithId(
            final Long id,
            final MemberRole role,
            final String password,
            final String name,
            final String email
    ) {
        return new Member(
                id,
                role,
                new MemberPassword(password),
                new MemberName(name),
                new MemberEmail(email)
        );
    }

    private Member(
            final Long id,
            final MemberRole role,
            final MemberPassword password,
            final MemberName name,
            final MemberEmail email
    ) {
        this.id = id;
        this.role = role;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public Member initializeIndex(final Long memberId) {
        return new Member(memberId, role, password, name, email);
    }

    public MemberEmail getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public MemberPassword getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
