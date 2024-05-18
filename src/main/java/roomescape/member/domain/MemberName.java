package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.BadArgumentRequestException;

@Embeddable
public record MemberName(
        @Column(length = 20, nullable = false, unique = true)
        String name) {

    private static final int MAX_LENGTH = 20;

    public MemberName {
        Objects.requireNonNull(name);
        if (name.isEmpty() || name.length() > MAX_LENGTH) {
            throw new BadArgumentRequestException("예약자 이름은 1글자 이상 20글자 이하이어야 합니다.");
        }
    }
}
