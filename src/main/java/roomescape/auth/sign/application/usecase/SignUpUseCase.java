package roomescape.auth.sign.application.usecase;

import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.domain.User;

public interface SignUpUseCase {

    User execute(SignUpRequest request);
}
