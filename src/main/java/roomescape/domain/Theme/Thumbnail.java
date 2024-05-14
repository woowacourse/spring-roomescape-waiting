package roomescape.domain.Theme;

public class Thumbnail {

    private final String thumbnail;

    public Thumbnail(String thumbnail) {
        validateThumbnail(thumbnail);
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail.isEmpty()) {
            throw new IllegalArgumentException(
                    "[ERROR] 썸네일은 1자 이상 가능합니다.",
                    new Throwable("theme_thumbnail : " + thumbnail)
            );
        }
    }
}
