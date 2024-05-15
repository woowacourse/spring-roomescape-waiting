package roomescape.member.domain;

public class Member {
    private Long id;
    private Role role;
    private final MemberName name;
    private final String email;
    private final String password;

    public Member(MemberName name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(Long id, Role role, MemberName name, String email, String password) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
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

    public Role getRole() {
        return role;
    }
}
