package roomescape.member.model;

public record MemberName(String value) {

    private static final int MAXIMUM_ENABLE_NAME_LENGTH = 8;

    public MemberName {
        validateValue(value);
    }

    private void validateValue(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("회원 이름으로 공백을 입력할 수 없습니다.");
        }

        if (value.length() > MAXIMUM_ENABLE_NAME_LENGTH) {
            throw new IllegalArgumentException("회원 이름은 " + MAXIMUM_ENABLE_NAME_LENGTH + "글자 이하여만 합니다.");
        }
    }
}
