package roomescape.auth.sign.ui.dto;

import roomescape.auth.sign.application.dto.SignInRequest;
import roomescape.common.domain.Email;

public record SignInWebRequest(String email,
                               String password) {

    public SignInRequest toServiceRequest() {
        return new SignInRequest(
                Email.from(email),
                password);
    }
}
