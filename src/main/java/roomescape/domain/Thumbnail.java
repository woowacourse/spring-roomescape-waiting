package roomescape.domain;

import static roomescape.exception.ExceptionType.EMPTY_THUMBNAIL;
import static roomescape.exception.ExceptionType.NOT_URL_BASE_THUMBNAIL;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.RoomescapeException;

@Embeddable
public class Thumbnail {
    @Column(name = "thumbnail")
    private String url;

    public Thumbnail(String url) {
        validateThumbnail(url);
        this.url = url;
    }

    private void validateThumbnail(String url) {
        if (url == null || url.isBlank()) {
            throw new RoomescapeException(EMPTY_THUMBNAIL);
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new RoomescapeException(NOT_URL_BASE_THUMBNAIL);
        }
    }

    protected Thumbnail() {
    }

    public String getUrl() {
        return url;
    }
}
