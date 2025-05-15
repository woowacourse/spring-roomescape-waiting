package roomescape.user.domain;

import java.util.Arrays;
import roomescape.user.exception.InvalidRoleException;

public enum Role {

    ROLE_MEMBER("유저"),
    ROLE_ADMIN("관리자"),
    ;

    private final String koreanName;

    Role(String koreanName) {
        this.koreanName = koreanName;
    }

    public static Role findByName(String name) {
        return Arrays.stream(Role.values())
                .filter(o -> o.name().equals(name))
                .findFirst()
                .orElseThrow(InvalidRoleException::new);
    }

    public String getKoreanName() {
        return koreanName;
    }
}
