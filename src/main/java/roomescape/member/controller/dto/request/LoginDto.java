package roomescape.member.controller.dto.request;

import roomescape.common.validation.annotation.NotBlank;
import roomescape.member.service.dto.LoginCommand;

public record LoginDto(
    @NotBlank(message = "name을 입력해주세요.")
    String name,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(name, password);
    }
}
