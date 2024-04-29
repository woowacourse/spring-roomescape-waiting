package roomescape.domain.theme;

import java.util.Objects;
import roomescape.global.exception.RoomescapeException;

public class Thumbnail {

    private static final String PREFIX = "https://";

    private final String url;

    public Thumbnail(String url) {
        validate(url);
        this.url = url;
    }

    private void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new RoomescapeException(String.format("썸네일 URL은 %s로 시작해야 합니다.", PREFIX));
        }
        if (!url.startsWith(PREFIX)) {
            throw new RoomescapeException(String.format("썸네일 URL은 %s로 시작해야 합니다.", PREFIX));
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Thumbnail thumbnail = (Thumbnail) o;
        return Objects.equals(url, thumbnail.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
