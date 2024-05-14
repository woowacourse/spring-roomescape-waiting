package roomescape.domain.member;

public class Member {

    private final Long id;
    private final String email;
    private final MemberPassword password;
    private final String name;
    private final Role role;

    public Member(Long id, String email, MemberPassword password, String name, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Member(Long id, String email, String password, String name, String role) {
        this(
                id,
                email,
                new MemberPassword(password),
                name,
                Role.getRole(role)
        );
    }

    public Member(Long id, String email, String password, String name) {
        this(
                id,
                email,
                new MemberPassword(password),
                name,
                Role.MEMBER
        );
    }

    public boolean isMismatchedPassword(MemberPassword other) {
        return this.password.isMismatchedPassword(other);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password.getValue();
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
