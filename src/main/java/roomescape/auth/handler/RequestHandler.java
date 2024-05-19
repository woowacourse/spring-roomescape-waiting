package roomescape.auth.handler;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestHandler {
    String extract(HttpServletRequest request);
}
