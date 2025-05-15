package roomescape.auth.dto;

import roomescape.member.MemberRole;

public record LoginMember(String name, String email, MemberRole role) {
}
