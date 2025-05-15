package roomescape.member.controller.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record SignupRequest(
        String email,
        String password,
        String name
) {

    public SignupRequest {
        validate(email, password, name);
    }

    private void validate(final String email, final String password, final String name) {
        Validator.of(SignupRequest.class)
                .notNullField(Fields.email, email)
                .notNullField(Fields.password, password)
                .notBlankField(Fields.name, name);
    }
}
