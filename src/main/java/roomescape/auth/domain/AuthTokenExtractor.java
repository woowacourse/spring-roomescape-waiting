package roomescape.auth.domain;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthTokenExtractor<T> {

    String AUTH_TOKEN_NAME = "token";

    @Nullable
    T extract(HttpServletRequest request);
}
