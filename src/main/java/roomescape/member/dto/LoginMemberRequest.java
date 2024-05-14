package roomescape.member.dto;

import roomescape.member.domain.LoginMember;

public record LoginMemberRequest(Long id, String name, String email, String role) {

    public LoginMemberRequest(LoginMember loginMember) {
        this(
                loginMember.getId(),
                loginMember.getName().name(),
                loginMember.getEmail().email(),
                loginMember.getRole().getDbValue()
        );
    }

    public LoginMember toLoginMember() {
        return new LoginMember(id, name, email, role);
    }
}
