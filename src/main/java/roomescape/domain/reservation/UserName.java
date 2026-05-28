package roomescape.domain.reservation;

import roomescape.common.exception.ForbiddenException;

public record UserName(
        String value
) {
    public static final int NAME_MAX_LENGTH = 10;

    public UserName {
        if (value.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 비어 있을 수 없습니다.");
        }

        if (value.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 이름은 %d자를 초과할 수 없습니다.".formatted(NAME_MAX_LENGTH));
        }
    }

    public static UserName parse(String value) {
        return new UserName(value);
    }

    public void validateOwner(String name) {
        if (!value.equals(name)) {
            throw new ForbiddenException("다른 사람의 예약은 취소/변경할 수 없습니다.");
        }
    }
}
