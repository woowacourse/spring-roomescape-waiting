package roomescape.member.controller.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record LoginRequest(
        String email,
        String password
) {

    public LoginRequest {
        validate(email, password);
    }

    private void validate(final String email, final String password) {
        Validator.of(LoginRequest.class)
                .notNullField(Fields.email, email)
                .notNullField(Fields.password, password);
    }
}
