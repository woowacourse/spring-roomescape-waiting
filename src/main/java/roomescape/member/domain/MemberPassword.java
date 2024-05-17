package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public record MemberPassword(
        @Column(name = "password", length = 100, nullable = false)
        String value) {
    private static final int MAX_LENGTH = 100;

    public MemberPassword {
        Objects.requireNonNull(value);
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("예약자 비밀번호는 1글자 이상 100글자 이하이어야 합니다.");
        }
    }
}
