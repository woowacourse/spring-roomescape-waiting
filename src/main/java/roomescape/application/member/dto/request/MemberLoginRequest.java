package roomescape.application.member.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.member.Email;
import roomescape.domain.member.Password;

public record MemberLoginRequest(
        @NotNull(message = "이메일을 입력해주세요.")
        Email email,
        @NotNull(message = "비밀번호를 입력해주세요.")
        Password password) {

    public MemberLoginRequest(String email, String password) {
        this(new Email(email), new Password(password));
    }
}
