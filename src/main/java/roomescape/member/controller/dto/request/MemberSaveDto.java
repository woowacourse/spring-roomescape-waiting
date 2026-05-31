package roomescape.member.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.member.service.dto.MemberSaveCommand;

public record MemberSaveDto(
        @NotBlank(message = "name을 입력해주세요.")
        String name,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {

    public MemberSaveCommand toCommand() {
        return new MemberSaveCommand(name, password);
    }

}
