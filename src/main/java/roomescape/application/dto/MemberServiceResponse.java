package roomescape.application.dto;

import java.util.List;
import roomescape.domain.Role;
import roomescape.domain.entity.Member;

public record MemberServiceResponse(
        long id,
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
}
