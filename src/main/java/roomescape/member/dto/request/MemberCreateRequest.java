package roomescape.member.dto.request;

import roomescape.global.exception.InvalidInputException;

public record MemberCreateRequest(String email, String password, String name) {

    public MemberCreateRequest {
        validateNull(email, password, name);
    }

    private void validateNull(String email, String password, String name) {
        if (email == null) {
            throw new InvalidInputException("이메일이 입력되지 않았다.");
        }
        if (password == null) {
            throw new InvalidInputException("패스워드가 입력되지 않았다.");
        }
        if (name == null) {
            throw new InvalidInputException("이름이 입력되지 않았다.");
        }
    }
}
