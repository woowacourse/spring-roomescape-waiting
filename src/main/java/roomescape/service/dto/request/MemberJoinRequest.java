package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.domain.Member;

public record MemberJoinRequest(
        // TODO 전반적인 메세지 처리 일관성

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 30)
        String password,

        @NotBlank
        @Size(max = 15)
        String name
) {
        public Member toUserMember() {
                return Member.createUser(name, email, password);
        }
}
