package roomescape.auth.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthorizationManager {

    String getAuthorization(HttpServletRequest httpServletRequest);

    void setAuthorization(HttpServletResponse httpServletResponse, final String token);
}
