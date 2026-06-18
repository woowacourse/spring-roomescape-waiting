package roomescape.domain.theme;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ThumbnailUrl {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");

    @Column(name = "thumbnail_url", nullable = false)
    private String value;

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
}
