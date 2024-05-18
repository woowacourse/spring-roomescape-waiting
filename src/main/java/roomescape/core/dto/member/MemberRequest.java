package roomescape.core.dto.member;

public class MemberRequest {
    private String email;
    private String password;
    private String name;

    public MemberRequest() {
    }

    public MemberRequest(final String email, final String password, final String name) {
        this.email = email;
        this.password = password;
        this.name = name;
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
}
