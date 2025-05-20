package roomescape.auth.sign.application;

import jakarta.servlet.http.Cookie;
import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.auth.sign.ui.dto.UserSessionResponse;
import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.domain.UserId;

import java.util.function.Consumer;

public interface SignFacade {

    void signIn(SignInRequest request, Consumer<Cookie> cookieSetter);

    UserId signUp(SignUpRequest request);
}
