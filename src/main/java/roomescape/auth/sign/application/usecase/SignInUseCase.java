package roomescape.auth.sign.application.usecase;

import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.auth.sign.application.dto.SignInResult;

public interface SignInUseCase {

    SignInResult execute(SignInRequest request);
}
