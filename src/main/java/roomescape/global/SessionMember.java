package roomescape.global;

import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;

public record SessionMember(Long id, MemberName name, MemberRole role) {
}
