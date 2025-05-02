package roomescape.domain.member;


public class Member {

    private final Long id;
    private final String username;
    private final String password;
    private final Role role;
    private final String name;

    public Member(Long id, String username, String password, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public static Member of(long id, Member member) {
        return new Member(id, member.username, member.password, member.name, member.role);
    }

    public boolean isSamePassword(String password) {
        return this.password.equals(password);
    }

    public boolean isSameUsername(String username) {
        return this.username.equals(username);
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
