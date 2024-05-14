package roomescape.service.dto;

import roomescape.domain.Role;

public record LoginMember(
        Long id,
        String name,
        Role role
) {
    public MemberResponse toMemberResponse() {
        return new MemberResponse(id, name, role.name());
    }
}
