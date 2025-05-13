package roomescape.business.domain;

public class Member {

    private final Long id;
    private final String name;
    private final String role;
    private final String email;
    private final String password;

    public Member(final Long id, final String name, final String role, final String email, final String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public Member(final Long id){
        this.id = id;
        this.name = null;
        this.role = null;
        this.email = null;
        this.password = null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
