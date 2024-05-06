package roomescape.application.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.member.Member;

public record MemberRegisterRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        String name,
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password) {

    public Member toMember() {
        return new Member(name, email, password);
    }
}
