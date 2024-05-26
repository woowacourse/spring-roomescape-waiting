package roomescape.service.dto.request;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public record SignupRequest(String email, String password, String name) {
    public SignupRequest {
        validate(email, password, name);
    }

    private void validate(String email, String password, String name) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public Member toMember(MemberRole role) {
        return new Member(name, email, password, role);
    }
}
