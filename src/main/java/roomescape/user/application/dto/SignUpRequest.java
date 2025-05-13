package roomescape.user.application.dto;

import roomescape.common.domain.Email;
import roomescape.user.domain.UserName;

public record SignUpRequest(UserName name,
                            Email email,
                            String rawPassword) {

}
