package roomescape.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminRequestValidator {

    private static final String AUTH_HEADER = "X-ADMIN-TOKEN";

    @Value("${token}")
    private String adminToken;

    public boolean isUnauthorized(HttpServletRequest request) {
        String token = request.getHeader(AUTH_HEADER);
        return !adminToken.equals(token);
    }
}
