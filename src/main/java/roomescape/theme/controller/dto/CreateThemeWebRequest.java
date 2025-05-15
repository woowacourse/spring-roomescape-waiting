package roomescape.theme.controller.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateThemeWebRequest(
        String name,
        String description,
        String thumbnail
) {

    public CreateThemeWebRequest {
        validate(name, description, thumbnail);
    }

    private void validate(final String name, final String description, final String thumbnail) {
        Validator.of(CreateThemeWebRequest.class)
                .notNullField(Fields.name, name)
                .notNullField(Fields.description, description)
                .notNullField(Fields.thumbnail, thumbnail);
    }
}
