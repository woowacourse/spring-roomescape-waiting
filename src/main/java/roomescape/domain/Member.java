package roomescape.domain;

public class Member {

    private final Long id;
    private final MemberRole role;
    private final String email;
    private final String password;
    private final String name;
    private final String sessionId;

    public Member(final Long id, final MemberRole role, final String email, final String password, final String name, final String sessionId) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.password = password;
        this.name = name;
        this.sessionId = sessionId;
    }

    public static Member createWithOutIdAndSession(final String email, final MemberRole role, final String password, final String name) {
        return new Member(null, role, email, password, name, null);
    }

    public Member createWithId(final long id) {
        return new Member(id, role, email, password, name, sessionId);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public MemberRole getRole() {
        return role;
    }
}
