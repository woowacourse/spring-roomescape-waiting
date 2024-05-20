package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class MemberName {
    public static final int NAME_MAX_LENGTH = 20;

    @Column(nullable = false)
    private String name;

    protected MemberName() {
    }

    public MemberName(String name) {
        validateNonBlank(name);
        validateLength(name);
        this.name = name;
    }

    private void validateNonBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자명은 필수 입력값 입니다.");
        }
    }

    private void validateLength(String name) {
        if (name != null && name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("예약자명은 %d자 이하여야 합니다.", NAME_MAX_LENGTH));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberName memberName)) {
            return false;
        }
        return Objects.equals(this.name, memberName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public String getName() {
        return name;
    }
}
