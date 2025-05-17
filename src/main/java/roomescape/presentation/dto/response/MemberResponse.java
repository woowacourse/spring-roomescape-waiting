package roomescape.presentation.dto.response;

import roomescape.domain.Member;
import roomescape.presentation.dto.request.LoginMember;

import java.util.List;

public record MemberResponse(Long id, String name, String email) {

    public static MemberResponse from(LoginMember loginMember) {
        return new MemberResponse(loginMember.id(), loginMember.name(), loginMember.email());
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public static List<MemberResponse> from(List<Member> members) {
        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }
}
