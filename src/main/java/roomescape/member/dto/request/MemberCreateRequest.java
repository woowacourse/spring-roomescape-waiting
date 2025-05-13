package roomescape.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(
        @NotBlank(message = "이메일이 입력되지 않았다.") String email,
        @NotBlank(message = "패스워드가 입력되지 않았다.") String password,
        @NotBlank(message = "이름이 입력되지 않았다.") String name) {

}
