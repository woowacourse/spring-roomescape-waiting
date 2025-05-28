package roomescape.global.auth;

import roomescape.member.domain.Role;

public record LoginMember(long id, Role role, String name) {
}
