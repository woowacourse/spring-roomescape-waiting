package roomescape.reservation.model;

public record ThemeThumbnail(String value) {

    private static final int MAXIMUM_ENABLE_NAME_LENGTH = 700;

    public ThemeThumbnail {
        validateValue(value);
    }

    private void validateValue(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("썸네일은 1글자 이상 700글자 이하여야 합니다.");
        }

        final String stripedValue = value.strip();
        if (stripedValue.isEmpty() || stripedValue.length() > MAXIMUM_ENABLE_NAME_LENGTH) {
            throw new IllegalArgumentException("썸네일은 1글자 이상 700글자 이하여야 합니다.");
        }
    }
}
