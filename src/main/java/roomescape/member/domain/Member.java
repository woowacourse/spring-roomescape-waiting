package roomescape.member.domain;

public class Member {

    private final Long id;
    private final MemberName name;
    private final String email;
    private final String password;
    private final Role role;

    private Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member createWithId(Long id, String name, String email, String password, Role role) {
        return new Member(id, name, email, password, role);
    }

    public static Member createWithoutId(String name, String email, String password, Role role) {
        return new Member(null, name, email, password, role);
    }

    public Member assignId(Long id) {
        return new Member(id, name.getName(), email, password, role);
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

    public String getRole() {
        return role.getRole();
    }

    public Long getId() {
        return id;
    }
}
