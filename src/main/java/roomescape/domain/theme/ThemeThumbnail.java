package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public record ThemeThumbnail(
        @Column(nullable = false)
        @Size(max = ThemeThumbnail.MAX_THUMBNAIL_LENGTH)
        String thumbnail
) {
    private static final int MAX_THUMBNAIL_LENGTH = 50;

    public ThemeThumbnail(final String thumbnail) {
        this.thumbnail = Objects.requireNonNull(thumbnail, "thumbnail은 null일 수 없습니다.");
        if (thumbnail.length() > MAX_THUMBNAIL_LENGTH) {
            throw new IllegalStateException("thumbnail은 " + MAX_THUMBNAIL_LENGTH + "자 이내여야 합니다.");
        }
    }
}
