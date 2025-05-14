package roomescape.member.domain;

public record MemberName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 10;

    public MemberName {
        validate(value);
    }

    private void validate(final String value) {
        validateBlank(value);
        validateLength(value);
    }

    private void validateBlank(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("값이 존재하지 않습니다.");
        }
    }

    private void validateLength(final String value) {
        int length = value.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("사용자 이름은 %d자 이상 %d자 이하로 가능합니다.".formatted(
                    MIN_LENGTH, MAX_LENGTH
            ));
        }
    }
}
