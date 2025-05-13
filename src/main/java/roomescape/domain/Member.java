package roomescape.domain;

public class Member {
    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    private Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member of(Long id, String name, String email, String password, Role role) {
        return new Member(id, name, email, password, role);
    }

    public static Member of(Long id, String name, String email, String password) {
        return of(id, name, email, password, Role.USER);
    }

    public static Member withoutId(String name, String email, String password, Role role) {
        return of(null, name, email, password, role);
    }

    public static Member assignId(Long id, Member memberWithoutId) {
        return new Member(id, memberWithoutId.getName(), memberWithoutId.getEmail(), memberWithoutId.getPassword(),
                memberWithoutId.getRole());
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

    public Role getRole() {
        return role;
    }
}
