package roomescape.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record MemberName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 10;

    public MemberName {
        validate(value);
    }

    private void validate(final String name) {
        validateBlank(name);
        validateLength(name);
    }

    private void validateBlank(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("값이 존재하지 않습니다.");
        }
    }

    private void validateLength(final String name) {
        int length = name.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("사용자 이름은 %d자 이상 %d자 이하로 가능합니다.".formatted(
                    MIN_LENGTH, MAX_LENGTH
            ));
        }
    }
}
