package roomescape.persistence.query;

import roomescape.domain.MemberRole;

public record CreateMemberQuery(String name, MemberRole role, String email, String password) {
}
