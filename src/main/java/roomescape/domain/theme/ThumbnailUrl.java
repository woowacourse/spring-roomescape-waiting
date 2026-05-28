package roomescape.domain.theme;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.util.Objects;
import java.util.regex.Pattern;

public class ThumbnailUrl {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");

    private final String value;

    public ThumbnailUrl(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        Objects.requireNonNull(value);
        if (!URL_PATTERN.matcher(value).matches()) {
            throw new RoomEscapeException(ErrorCode.INVALID_THUMBNAIL_URL);
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThumbnailUrl that = (ThumbnailUrl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
