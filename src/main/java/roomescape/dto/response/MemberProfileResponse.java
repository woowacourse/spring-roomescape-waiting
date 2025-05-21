package roomescape.dto.response;

import roomescape.domain.Member;
import roomescape.dto.business.AccessTokenContent;

public record MemberProfileResponse(
        Long id,
        String roleName,
        String name
) {

    public MemberProfileResponse(Member member) {
        this(member.getId(), member.getRole().toString(), member.getName());
    }

    public MemberProfileResponse(AccessTokenContent accessTokenContent) {
        this(accessTokenContent.id(), accessTokenContent.role().toString(), accessTokenContent.name());
    }
}
