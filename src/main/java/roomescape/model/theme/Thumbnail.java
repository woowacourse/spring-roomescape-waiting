package roomescape.model.theme;

public class Thumbnail {

    private final String value;

    public Thumbnail(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        validateNull(value);
    }

    private void validateNull(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("테마 썸네일은 null 혹은 빈 문자열일 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
