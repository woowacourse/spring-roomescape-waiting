package roomescape.reservation.domain;

import roomescape.global.exception.RoomEscapeException;

public record MemberName(
        String name
) {
    public MemberName {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("이름은 비어있을 수 없습니다.");
        }
    }
}
