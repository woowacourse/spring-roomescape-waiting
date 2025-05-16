package roomescape.auth.infrastructure.methodargument;

import roomescape.member.domain.Member;

public record MemberPrincipal(
    String name
) {

    public static MemberPrincipal fromMember(Member member) {
        return new MemberPrincipal(member.getName());
    }
}
