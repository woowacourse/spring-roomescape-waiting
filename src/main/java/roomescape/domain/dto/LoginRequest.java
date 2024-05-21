package roomescape.domain.dto;

import roomescape.exception.clienterror.EmptyValueNotAllowedException;

public record LoginRequest(String email, String password) {
    public LoginRequest {
        validEmpty("email", email);
        validEmpty("password", password);
    }

    private void validEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
        }
    }
}
