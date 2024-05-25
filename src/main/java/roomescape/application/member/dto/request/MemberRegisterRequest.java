package roomescape.application.member.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.Password;

public record MemberRegisterRequest(
        @NotNull(message = "이름을 입력해주세요.")
        MemberName name,
        @NotNull(message = "이메일을 입력해주세요.")
        Email email,
        @NotNull(message = "비밀번호를 입력해주세요.")
        Password password
) {
    public MemberRegisterRequest(String name, String email, String password) {
        this(new MemberName(name), new Email(email), new Password(password));
    }

    public Member toMember() {
        return new Member(name, email, password);
    }
}
