package roomescape.reservation.domain;

import lombok.Builder;
import roomescape.global.exception.RoomEscapeException;

public record User(String name) {

    @Builder
    public User {
        name = requireName(name);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("이름은 비어있을 수 없습니다.");
        }
        return name;
    }
}
