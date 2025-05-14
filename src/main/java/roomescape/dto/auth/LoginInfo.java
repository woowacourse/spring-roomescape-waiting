package roomescape.dto.auth;

import roomescape.domain.member.Role;

public record LoginInfo(long id, String name, String email, Role role) {

}
