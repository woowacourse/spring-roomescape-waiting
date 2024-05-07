package roomescape.model.member;

public class Password {

    private final String value;

    public Password(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        validateNull(value);
    }

    private void validateNull(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("비밀번호는 null 혹은 빈 문자열일 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
