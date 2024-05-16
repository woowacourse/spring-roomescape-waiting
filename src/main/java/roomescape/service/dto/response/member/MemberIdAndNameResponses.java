package roomescape.service.dto.response.member;

import java.util.List;
import roomescape.domain.Member;

public record MemberIdAndNameResponses(List<MemberIdAndNameResponse> members) {

    public static MemberIdAndNameResponses from(List<Member> members) {
        List<MemberIdAndNameResponse> responses = members.stream()
                .map(member -> new MemberIdAndNameResponse(member.getId(), member.getName()))
                .toList();
        return new MemberIdAndNameResponses(responses);
    }
}
