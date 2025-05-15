package roomescape.member.controller.response;

import java.util.List;
import roomescape.member.domain.Member;

public record MemberResponse(
        Long id, String email, String name
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName());
    }

    public static List<MemberResponse> from(List<Member> members) {
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }
}
