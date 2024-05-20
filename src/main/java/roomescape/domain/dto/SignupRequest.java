package roomescape.domain.dto;

import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

public record SignupRequest(String email, String password, String name) {
    public SignupRequest {
        validEmpty("email", email);
        validEmpty("password", password);
        validEmpty("name", name);
    }

    private void validEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
        }
    }

    public Member toEntity(Password password) {
        return new Member(email, password, name, Role.USER);
    }
}
