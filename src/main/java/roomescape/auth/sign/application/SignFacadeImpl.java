package roomescape.auth.sign.application;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.auth.sign.application.dto.SignInResult;
import roomescape.auth.sign.application.usecase.SignInUseCase;
import roomescape.auth.sign.application.usecase.SignUpUseCase;
import roomescape.auth.sign.ui.dto.UserSessionResponse;
import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class SignFacadeImpl implements SignFacade {

    private final SignInUseCase signInUseCase;
    private final SignUpUseCase signUpUseCase;

    @Override
    public void signIn(final SignInRequest request,
                       final Consumer<Cookie> cookieSetter) {
        final SignInResult result = signInUseCase.execute(request);
        cookieSetter.accept(result.cookie());
    }

    @Override
    public UserId signUp(final SignUpRequest request) {
        final User user = signUpUseCase.execute(request);
        return user.getId();
    }

    // TODO signout
}
