package roomescape.auth.sign.application;

import jakarta.servlet.http.HttpServletResponse;
import roomescape.auth.sign.ui.dto.SignInWebRequest;
import roomescape.auth.sign.ui.dto.SignUpWebRequest;
import roomescape.auth.sign.ui.dto.UserSessionResponse;

public interface SignFacade {

    void signIn(SignInWebRequest request, HttpServletResponse response);

    UserSessionResponse signUp(SignUpWebRequest request);
}
