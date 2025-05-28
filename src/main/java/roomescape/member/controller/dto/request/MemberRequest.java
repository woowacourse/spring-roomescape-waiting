package roomescape.member.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberRequest(
    @NotBlank @Pattern(regexp = "[^0-9]*", message = "숫자를 입력할 수 없습니다.")
    String name,

    @NotBlank @Email
    String email,

    @NotBlank String password
) {
}
