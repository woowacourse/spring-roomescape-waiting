package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record LoginMemberInfo(@NotNull Long id) {

    public static LoginMemberInfo of(Long id) {
        return new LoginMemberInfo(id);
    }
}
