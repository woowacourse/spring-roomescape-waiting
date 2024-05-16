package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class PlayerName {
    public static final int NAME_MAX_LENGTH = 20;

    private String name;

    PlayerName() {
    }

    public PlayerName(String name) {
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
        if (!(o instanceof PlayerName playerName)) {
            return false;
        }
        return Objects.equals(this.name, playerName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public String getName() {
        return name;
    }
}
