package roomescape.auth.handler;

import jakarta.servlet.http.HttpServletResponse;

public interface ResponseHandler {
    void set(HttpServletResponse response, String token);

    void expire(HttpServletResponse response);
}
