package roomescape.domain;

public class User {
    private final Long id;
    private final String username;
    private final Password password;
    private final String name;
    private final Role role;

    public User(String username, Password password, String name, Role role) {
        this(null, username, password, name, role);
    }

    private User(Long id, String username, Password password, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public User withId(Long id) {
        return new User(id, this.username, this.password, this.name, this.role);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Password getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getRoleName() {
        return role.name();
    }
}
