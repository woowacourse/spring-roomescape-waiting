package roomescape.domain;

import java.util.Arrays;
import roomescape.exception.BadRequestException;

public enum Role {
    ADMIN,
    MEMBER;

    public static Role findBy(String role) {
        return Arrays.stream(values()).filter(value -> value.name().equals(role))
                .findAny()
                .orElseThrow(() -> new BadRequestException("사용자의 역할이 존재하지 않습니다."));
    }
}
