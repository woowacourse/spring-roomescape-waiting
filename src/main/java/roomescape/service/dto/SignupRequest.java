package roomescape.service.dto;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public class SignupRequest {
    private final String email;
    private final String password;
    private final String name;

    public SignupRequest(String email, String password, String name) {
        validate(email, password, name);
        this.email = email;
        this.password = password;
        this.name = name;
    }

    private void validate(String email, String password, String name) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public Member toMember(MemberRole role) {
        return new Member(name, email, password, role);
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
