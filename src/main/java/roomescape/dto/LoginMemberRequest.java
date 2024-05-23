package roomescape.dto;

import roomescape.domain.Member;
import roomescape.domain.Name;
import roomescape.domain.Role;

public record LoginMemberRequest(long id, Name name, Role role) {
    public static LoginMemberRequest from(Member member) {
        return new LoginMemberRequest(member.getId(), member.getName(), member.getRole());
    }
}
