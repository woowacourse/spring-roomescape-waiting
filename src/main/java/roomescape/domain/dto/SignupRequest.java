package roomescape.domain.dto;

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
}
