package roomescape.application.dto;

import java.util.List;
import roomescape.domain.Member;
import roomescape.domain.Role;

public record MemberServiceResponse(
        Long id,
        String name,
        String email,
        String password,
        Role role
) {
    public static MemberServiceResponse from(Member member) {
        return new MemberServiceResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
    }

    public static List<MemberServiceResponse> from(List<Member> members) {
        return members.stream()
                .map(MemberServiceResponse::from)
                .toList();
    }

    public Member toEntity() {
        return Member.of(id, name, email, password, role);
    }
}
