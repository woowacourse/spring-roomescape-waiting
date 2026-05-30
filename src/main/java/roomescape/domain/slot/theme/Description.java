package roomescape.domain.slot.theme;

public record Description(
        String value
) {
    public static final int DESCRIPTION_NAME_MAX_LENGTH = 200;

    public Description {
        if (value.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 비어 있을 수 없습니다.");
        }

        if (value.length() > DESCRIPTION_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("테마 설명은 %d자를 초과할 수 없습니다.".formatted(DESCRIPTION_NAME_MAX_LENGTH));
        }
    }

    public static Description parse(String value) {
        return new Description(value);
    }
}
