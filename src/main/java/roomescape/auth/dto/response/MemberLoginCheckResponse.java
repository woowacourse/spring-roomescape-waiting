package roomescape.auth.dto.response;

import roomescape.auth.infrastructure.methodargument.MemberPrincipal;

public record MemberLoginCheckResponse(
    String name
) {

    public static MemberLoginCheckResponse fromMemberPrincipal(MemberPrincipal memberPrincipal) {
        return new MemberLoginCheckResponse(memberPrincipal.name());
    }
}
