package roomescape.model.member;

public class Name {

    private final String value;

    public Name(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        validateNull(value);
    }

    private void validateNull(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("사용자 이름은 null 혹은 빈 문자열일 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
