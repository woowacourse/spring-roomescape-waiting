package roomescape.application.member.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.domain.member.PlayerName;

public record MemberRegisterRequest(
        @NotNull(message = "이름을 입력해주세요.")
        PlayerName name,
        @NotNull(message = "이메일을 입력해주세요.")
        Email email,
        @NotNull(message = "비밀번호를 입력해주세요.")
        Password password) {

    public MemberRegisterRequest(String name, String email, String password) {
        this(new PlayerName(name), new Email(email), new Password(password));
    }

    public Member toMember() {
        return new Member(name, email, password);
    }
}
