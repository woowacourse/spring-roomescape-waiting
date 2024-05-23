package roomescape.service.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Member;
import roomescape.service.dto.AuthInfo;

public record MemberResponse(
        @NotNull
        Long id,
        @NotBlank
        String name) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }

    public static MemberResponse from(AuthInfo authInfo) {
        return new MemberResponse(authInfo.id(), authInfo.name());
    }
}
