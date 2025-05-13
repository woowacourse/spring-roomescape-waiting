package roomescape.member.controller.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record LoginCheckResponse(
        String name
) {

    public LoginCheckResponse {
        validate(name);
    }

    private void validate(final String name) {
        Validator.of(LoginCheckResponse.class)
                .notNullField(Fields.name, name);
    }
}
