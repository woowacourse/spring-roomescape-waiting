package roomescape.auth.sign.application.dto;

import roomescape.common.domain.Email;

public record SignInRequest(Email email,
                            String rawPassword) {

}
