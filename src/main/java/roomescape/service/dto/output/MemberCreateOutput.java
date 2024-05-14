package roomescape.service.dto.output;

import roomescape.domain.user.Member;

public record MemberCreateOutput(long id, String name, String email,String password) {
    public static MemberCreateOutput toOutput(final Member member) {
        return new MemberCreateOutput(member.getId(), member.getName(), member.getEmail(), member.getPassword());
    }
}
