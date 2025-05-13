package roomescape.theme.domain;

import java.util.Objects;

public final class ThemeThumbnail {

    private final String thumbnail;

    public ThemeThumbnail(final String thumbnail) {
        validateUrl(thumbnail);
        this.thumbnail = thumbnail;
    }

    private void validateUrl(final String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank() || thumbnail.length() > 255) {
            throw new IllegalArgumentException("썸네일은 최소 1글자, 최대 255글자여야합니다.");
        }
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ThemeThumbnail that = (ThemeThumbnail) o;
        return Objects.equals(thumbnail, that.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(thumbnail);
    }
}
