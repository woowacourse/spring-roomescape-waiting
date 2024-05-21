package roomescape.controller.dto;

import roomescape.system.exception.RoomescapeException;

public record LoginRequest(String email, String password) {

    public LoginRequest {
        validate(email, password);
    }

    private void validate(String email, String password) {
        if (email.isBlank() || password.isBlank()) {
            throw new RoomescapeException("이메일이나 비밀번호는 비어있을 수 없습니다.");
        }
    }
}
