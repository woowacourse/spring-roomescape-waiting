package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(@Email(message = "잘못된 이메일 형식입니다.")
                           @NotNull(message = "이메일을 입력해주세요") String email,
                           @NotNull(message = "비밀번호를 입력해주세요") String password) {
}
