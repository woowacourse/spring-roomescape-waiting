package roomescape.controller.dto.request;

import roomescape.exception.custom.InvalidInputException;

public record MemberRequest(
    String name,
    String email,
    String password) {

    public MemberRequest {
        validateNull(name, email, password);
        validateLengthOfString(name, email, password);
    }

    private void validateNull(String name, String email, String password) {
        if (name == null || email == null || password == null) {
            throw new InvalidInputException("선택되지 않은 값 존재");
        }
    }

    private void validateLengthOfString(String name, String email, String password) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw new InvalidInputException("입력되지 않은 값 존재");
        }
    }
}
