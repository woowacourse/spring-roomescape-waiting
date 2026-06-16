package roomescape.domain.theme;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class ThumbnailUrl {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");

    @Column(name = "thumbnail_url", nullable = false)
    private String value;

    protected ThumbnailUrl() {
    }

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
