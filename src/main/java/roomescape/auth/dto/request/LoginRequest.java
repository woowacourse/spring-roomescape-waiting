package roomescape.auth.dto.request;

import roomescape.global.exception.InvalidInputException;

public record LoginRequest(String email, String password) {

    public LoginRequest {
        validateNull(email, password);
    }

    private void validateNull(String email, String password) {
        if (email == null) {
            throw new InvalidInputException("이메일이 입력되지 않았다.");
        }
        if (password == null) {
            throw new InvalidInputException("패스워드가 입력되지 않았다.");
        }
    }
}
