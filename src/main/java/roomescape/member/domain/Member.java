package roomescape.member.domain;

public class Member {
    private final Long id;
    private final MemberName name;
    private final String email;
    private final String password;
    private final MemberRole role;

    public Member(final Long id, final String name, final String email, final String password, final MemberRole role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean hasSameId(final long other) {
        return id == other;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
