package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.global.Role;

public record LoginMemberRequest(
        @NotNull(message = "id는 비어있을 수 없습니다.")
        @Positive(message = "유효하지 않은 값입니다.")
        Long id,
        @NotBlank(message = "이름은 비어있을 수 없습니다.") String name,
        @NotNull(message = "유효하지 않은 값입니다.") Role role) {
}
