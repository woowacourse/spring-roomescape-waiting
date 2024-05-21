package roomescape.service.dto.request;

import roomescape.domain.Role;
import roomescape.service.dto.response.MemberResponse;

public record LoginMember(
        Long id,
        String name,
        Role role
) {
    public MemberResponse toMemberResponse() {
        return new MemberResponse(id, name, role.name());
    }
}
