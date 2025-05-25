package roomescape.reservation.domain.theme;

import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.common.exception.RoomescapeException;

@Embeddable
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "thumbnail")
public class ThemeThumbnail {

    private static final List<String> SUPPORTED_FORMATS = List.of("jpg", "jpeg", "png");

    private String thumbnail;

    public ThemeThumbnail(final String thumbnail) {
        validate(thumbnail);
        this.thumbnail = thumbnail;
    }

    private void validate(final String thumbnail) {
        validateMissing(thumbnail);
        validateFormat(thumbnail);
    }

    private void validateMissing(final String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new RoomescapeException("테마 썸네일은 null 또는 공백이 아니어야 합니다.");
        }
    }

    private void validateFormat(final String thumbnail) {
        final String lower = thumbnail.toLowerCase();
        final boolean matched = SUPPORTED_FORMATS.stream().anyMatch(lower::endsWith);
        if (!matched) {
            final String allowedFormats = String.join(", ", SUPPORTED_FORMATS);
            throw new RoomescapeException("테마 썸네일은 " + allowedFormats + " 형식만 허용됩니다.");
        }
    }
}
