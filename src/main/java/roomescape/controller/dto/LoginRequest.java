package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "이메일이나 비밀번호는 비어있을 수 없습니다.")
    String email,
    @NotBlank(message = "이메일이나 비밀번호는 비어있을 수 없습니다.")
    String password) {

}
