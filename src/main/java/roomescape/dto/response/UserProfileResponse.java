package roomescape.dto.response;

import roomescape.domain.Member;
import roomescape.dto.business.AccessTokenContent;

public record UserProfileResponse(
        Long id,
        String roleName,
        String name
) {

    public UserProfileResponse(Member member) {
        this(member.getId(), member.getRole().toString(), member.getName());
    }

    public UserProfileResponse(AccessTokenContent accessTokenContent) {
        this(accessTokenContent.id(), accessTokenContent.role().toString(), accessTokenContent.name());
    }
}
