package roomescape.service.dto.request;

import java.util.stream.Stream;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public record SignupRequest(String email, String password, String name) {
    public SignupRequest {
        validate(email, password, name);
    }

    private void validate(String... values) {
        if (Stream.of(values).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException();
        }
    }

    public Member toMember(MemberRole role) {
        return new Member(name, email, password, role);
    }
}
