package roomescape.domain.reservation;

import java.util.List;
import java.util.regex.Pattern;

public record Thumbnail(String value) {

    private static final List<String> ALLOW_EXTENSIONS = List.of("jpg", "jpeg", "png", "heic");
    public static final String ALLOW_EXTENSIONS_FORMAT = String.join("|", ALLOW_EXTENSIONS);
    private static final Pattern ALLOW_EXTENSIONS_PATTERN = Pattern.compile(
            String.format("^\\S+.(?i)(%s)$", ALLOW_EXTENSIONS_FORMAT));

    public Thumbnail {
        validate(value);
    }

    private void validate(final String url) {
        validateNull(url);
        validateExt(url);
    }

    private void validateNull(final String url) {
        if (url.isBlank()) {
            throw new IllegalArgumentException("경로는 공백일 수 없습니다!");
        }
    }

    private void validateExt(final String url) {
        if (!ALLOW_EXTENSIONS_PATTERN.matcher(url.toLowerCase()).matches()) {
            throw new IllegalArgumentException(String.format("%s 확장자만 가능합니다.", ALLOW_EXTENSIONS_FORMAT));
        }
    }

    public String asString() {
        return value;
    }
}
