package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import roomescape.member.exception.NameException;

@Embeddable
public record Name(String name) {

    public Name {
        validateNameIsNonEmpty(name);
        validateNameLength(name);
    }

    private static void validateNameIsNonEmpty(final String name) {
        if (name == null || name.isEmpty()) {
            throw new NameException("이름은 비어있을 수 없습니다.");
        }
    }

    private static void validateNameLength(String name) {
        if (name.isEmpty() || name.length() > 5) {
            throw new NameException("이름은 1-5글자 사이여야 합니다.");
        }
    }
}
