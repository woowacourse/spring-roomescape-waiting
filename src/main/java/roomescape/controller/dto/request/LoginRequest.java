package roomescape.controller.dto.request;

import roomescape.exception.custom.InvalidInputException;

public record LoginRequest(
    String email,
    String password) {

    public LoginRequest {
        validateNull(email, password);
        validateLengthOfString(email, password);
    }

    private void validateNull(String email, String password) {
        if (email == null || password == null) {
            throw new InvalidInputException("선택되지 않은 값 존재");
        }
    }

    private void validateLengthOfString(String email, String password) {
        if (email.isBlank() || password.isBlank()) {
            throw new InvalidInputException("입력되지 않은 값 존재");
        }
    }
}
