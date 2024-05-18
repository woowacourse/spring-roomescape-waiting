package roomescape.service.dto;

import roomescape.domain.member.Role;

public record LoginMember(
        Long id,
        String name,
        Role role
) {
    public MemberResponse toMemberResponse() {
        return new MemberResponse(id, name, role.name());
    }

    public boolean isNotSameId(Long id) {
        return !id.equals(this.id);
    }

    public boolean isUser() {
        return role == Role.USER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
