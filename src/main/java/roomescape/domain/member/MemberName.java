package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public record MemberName(
        @Column(nullable = false)
        @Size(max = MemberName.MAXIMUM_NAME_LENGTH)
        String name
) {
    public static final int MAXIMUM_NAME_LENGTH = 5;

    public MemberName(final String name) {
        this.name = Objects.requireNonNull(name, "name은 null일 수 없습니다.");
        if (name.isBlank()) {
            throw new IllegalStateException("사용자 이름은 공백일 수 없습니다.");
        }
        if (name.length() > MAXIMUM_NAME_LENGTH) {
            throw new IllegalStateException("사용자 이름은 5자 이하여야 합니다.");
        }
    }
}
