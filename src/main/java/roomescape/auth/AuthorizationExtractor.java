package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthorizationExtractor<T> {
    String TOKEN_NAME = "token";

    T extract(HttpServletRequest request);
}
