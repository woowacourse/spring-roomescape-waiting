package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank(message = "null이거나 비어있을 수 없습니다.")
    @Pattern(regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$",
        message = "올바르지 않은 이메일 형식입니다.")
    String email,

    @NotBlank(message = "null이거나 비어있을 수 없습니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{1,20}$",
        message = "패스워드는 1자 이상 20자 이하의 영문, 숫자, 기호 조합이어야 합니다.")
    String password
) { }
