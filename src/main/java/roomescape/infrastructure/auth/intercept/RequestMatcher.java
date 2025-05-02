package roomescape.infrastructure.auth.intercept;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestMatcher {

    boolean isMatch(HttpServletRequest request);
}
