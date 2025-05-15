package roomescape.auth;

import roomescape.business.model.vo.UserRole;

public record LoginInfo(
        String id,
        UserRole userRole
) {
}
