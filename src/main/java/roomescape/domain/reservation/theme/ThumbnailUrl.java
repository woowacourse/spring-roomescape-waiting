package roomescape.domain.reservation.theme;

import java.util.regex.Pattern;

public record ThumbnailUrl(
        String value
) {
    public static final Pattern THUMBNAIL_URL_PATTERN = Pattern.compile("^/images/.+");

    public ThumbnailUrl {
        if (value.isBlank()) {
            throw new IllegalArgumentException("썸네일 주소는 비어 있을 수 없습니다.");
        }

        if (!THUMBNAIL_URL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("올바른 썸네일 주소 형식이 아닙니다.");
        }
    }

    public static ThumbnailUrl parse(String value) {
        return new ThumbnailUrl(value);
    }
}
