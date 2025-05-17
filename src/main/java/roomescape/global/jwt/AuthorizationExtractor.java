package roomescape.global.jwt;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthorizationExtractor {
    String extract(HttpServletRequest request);
}
