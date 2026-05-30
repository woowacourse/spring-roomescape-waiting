package roomescape.feature.theme.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.springframework.util.StringUtils;
import roomescape.feature.theme.error.type.ThemeErrorType;
import roomescape.global.error.exception.GeneralException;

public record ThemeImageUrl(String value) {

    private static final int MAXIMUM_LENGTH = 2000;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public ThemeImageUrl {
        if (!StringUtils.hasText(value) || value.length() > MAXIMUM_LENGTH || !isValidUrl(value)) {
            throw new GeneralException(ThemeErrorType.INVALID_IMAGE_URL);
        }
    }

    private static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return scheme != null && ALLOWED_SCHEMES.contains(scheme) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
