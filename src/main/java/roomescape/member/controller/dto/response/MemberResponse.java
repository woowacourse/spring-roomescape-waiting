package roomescape.member.controller.dto.response;

import java.util.List;
import roomescape.member.domain.Member;

public record MemberResponse(
        long id,
        String name
) {

    public static MemberResponse from(final Member member) {
        return new MemberResponse(member.getId(), member.getNameValue());
    }

    public static List<MemberResponse> list(List<Member> members) {
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }
}
