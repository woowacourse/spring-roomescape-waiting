package roomescape.service.dto.member;

public class MemberLoginRequest {

    private final String email;
    private final String password;

    public MemberLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
