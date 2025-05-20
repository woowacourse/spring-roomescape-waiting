package roomescape.auth.web.support;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthorizationExtractor<T> {

    T extract(HttpServletRequest request);
}
