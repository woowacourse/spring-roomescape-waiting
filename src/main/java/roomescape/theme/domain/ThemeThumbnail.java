package roomescape.theme.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ThemeThumbnail {

    private static final String THUMBNAIL_URL_FORMAT = "(http|https).*";

    private String thumbnail;

    protected ThemeThumbnail() {
    }

    public ThemeThumbnail(String thumbnail) {
        validateThumbnailFormat(thumbnail);
        this.thumbnail = thumbnail;
    }

    private void validateThumbnailFormat(String thumbnail) {
        if (!thumbnail.matches(THUMBNAIL_URL_FORMAT)) {
            throw new IllegalArgumentException("썸네일은 url 링크여야합니다.");
        }
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
