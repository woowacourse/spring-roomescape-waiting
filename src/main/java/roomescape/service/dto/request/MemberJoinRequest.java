package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.domain.Member;

public record MemberJoinRequest(
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식에 맞게 입력해주세요")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(max = 30, message = " 비밀번호 길이는 최대 30자에요")
        String password,

        @NotBlank(message = "회원 이름을 입력해주세요")
        @Size(max = 15, message = "이름은 최대 15자까지 가능해요")
        String name
) {
    public Member toUserMember() {
        return Member.createUser(name, email, password);
    }
}
