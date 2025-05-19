package roomescape.auth.sign.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.domain.Email;
import roomescape.common.validate.Validator;
import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.domain.UserName;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record SignUpWebRequest(String name,
                               String email,
                               String password /* raw */) {

    public SignUpWebRequest {
        validate(name, email, password);
    }

    public SignUpRequest toServiceRequest() {
        return new SignUpRequest(
                UserName.from(name),
                Email.from(email),
                password
        );
    }

    private void validate(final String userName,
                          final String email,
                          final String password) {
        Validator.of(SignUpWebRequest.class)
                .validateNotNull(Fields.name, userName, DomainTerm.USER_NAME.label())
                .validateNotNull(Fields.email, email, DomainTerm.USER_EMAIL.label())
                .validateNotNull(Fields.password, password, DomainTerm.USER_PASSWORD.label());
    }
}
