package roomescape.reservation.domain.theme;

import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.reservation.domain.util.ValidationUtils;

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
        ValidationUtils.validateNonNull(thumbnail, "테마 썸네일은 필수입니다.");
        ValidationUtils.validateNonBlank(thumbnail, "테마 썸네일은 공백이 아니어야 합니다.");
        validateFormat(thumbnail);
    }

    private void validateFormat(final String thumbnail) {
        final String lower = thumbnail.toLowerCase();
        final boolean matched = SUPPORTED_FORMATS.stream().anyMatch(lower::endsWith);
        if (!matched) {
            final String allowedFormats = String.join(", ", SUPPORTED_FORMATS);
            throw new IllegalArgumentException("테마 썸네일은 " + allowedFormats + " 형식만 허용됩니다.");
        }
    }
}
