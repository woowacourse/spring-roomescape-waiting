package roomescape.service.response;

import java.util.List;
import roomescape.domain.member.Member;

public record MemberResponse(Long id, String name) {

    public static MemberResponse from(final Member member) {
        return new MemberResponse(member.getId(), member.getName().name());
    }

    public static List<MemberResponse> from(final List<Member> members) {
        return members.stream().map(MemberResponse::from).toList();
    }
}
