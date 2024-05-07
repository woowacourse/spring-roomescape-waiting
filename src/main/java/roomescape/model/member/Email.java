package roomescape.model.member;

public class Email {

    private final String value;

    public Email(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        validateNull(value);
        validateFormat(value);
    }

    private void validateNull(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("이메일은 null 혹은 빈 문자열일 수 없습니다.");
        }
    }

    private void validateFormat(String value) {
        if (!value.matches("^.+@.+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalStateException("이메일은 올바른 형식이어야 합니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
