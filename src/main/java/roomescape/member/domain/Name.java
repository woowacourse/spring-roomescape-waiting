package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.member.exception.NameException;

@Embeddable
public record Name(@Column(length = MAX_NAME_LENGTH, nullable = false) String name) {

    public static final int MAX_NAME_LENGTH = 5;

    public Name {
        validateNameIsNonEmpty(name);
        validateNameLength(name);
    }

    private void validateNameIsNonEmpty(final String name) {
        if (name == null || name.isEmpty()) {
            throw new NameException("이름은 비어있을 수 없습니다.");
        }
    }

    private void validateNameLength(String name) {
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            throw new NameException("이름은 1-5글자 사이여야 합니다.");
        }
    }
}
