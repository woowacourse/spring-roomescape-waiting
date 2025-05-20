package roomescape.infrastructure.auth.intercept;

import jakarta.servlet.http.HttpServletRequest;

public class AdminRequestMatcher implements RequestMatcher {

    private static final String startURI = "/admin";

    @Override
    public boolean isMatch(HttpServletRequest request) {
        return request.getRequestURI().startsWith(startURI);
    }
}
