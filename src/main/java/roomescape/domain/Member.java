package roomescape.domain;

public class Member {
    private Long id;
    private String name;
    private String email;
    private String password;
    private MemberRole role;

    public Member(Long id, String name, String email, String password, MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(String name, String email, String password, MemberRole role) {
        this(null, name, email, password, role);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
