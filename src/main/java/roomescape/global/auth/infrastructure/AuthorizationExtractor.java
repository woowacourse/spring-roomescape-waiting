package roomescape.global.auth.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class AuthorizationExtractor {

    private static final String COOKIE = "Cookie";
    private static final String AUTHORIZATION_KEY = "token=";

    public String extract(final NativeWebRequest webRequest) {
        String cookie = webRequest.getHeader(COOKIE);
        if (cookie != null && cookie.toLowerCase().startsWith(AUTHORIZATION_KEY.toLowerCase())) {
            return extractToken(cookie);
        }
        return null;
    }

    public String extract(final HttpServletRequest httpServletRequest) {
        Enumeration<String> headers = httpServletRequest.getHeaders(COOKIE);
        while (headers.hasMoreElements()) {
            String cookie = headers.nextElement();
            if (cookie.toLowerCase().startsWith(AUTHORIZATION_KEY.toLowerCase())) {
                return extractToken(cookie);
            }
        }
        return null;
    }

    private String extractToken(final String cookie) {
        String authHeaderValue = cookie.substring(AUTHORIZATION_KEY.length()).trim();
        int semicolonIndex = authHeaderValue.indexOf(";");
        if (semicolonIndex > 0) {
            authHeaderValue = authHeaderValue.substring(0, semicolonIndex);
        }
        return authHeaderValue;
    }
}
