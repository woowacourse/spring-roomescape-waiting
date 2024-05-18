package roomescape.service.dto;

import static roomescape.domain.Role.ADMIN;

import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;

public record AuthInfo(Long id, String name, Role role) {

    public AuthInfo {
        if (id == null) {
            throw new AuthenticationException("토큰으로부터 불러온 회원 ID가 존재하지 않습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new AuthenticationException("토큰으로부터 불러온 회원 이름이 존재하지 않습니다.");
        }
        if (role == null) {
            throw new AuthenticationException("토큰으로부터 불러온 회원 권한이 존재하지 않습니다.");
        }
    }

    public boolean isAdmin() {
        return role == ADMIN;
    }
}
