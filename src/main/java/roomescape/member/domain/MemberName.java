package roomescape.member.domain;

import java.util.Objects;

public record MemberName(String value) {
    private static final int MAX_LENGTH = 20;

    public MemberName {
        Objects.requireNonNull(value);
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 이름은 1글자 이상 20글자 이하이어야 합니다.");
        }
    }
}
