package roomescape.auth.dto;

import roomescape.member.domain.MemberRole;

public record LoginMember(String name, String email, MemberRole role) {
}
