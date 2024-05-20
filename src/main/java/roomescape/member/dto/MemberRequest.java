package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberRequest(Long id, String name, String email, String role, String password) {

    public MemberRequest(Member member) {
        this(
                member.getId(),
                member.getName().name(),
                member.getEmail().email(),
                member.getRole().name(),
                member.getPassword().password()
        );
    }

    public Member toMember() {
        return new Member(id, name, email, role, password);
    }
}
