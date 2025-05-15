package roomescape.theme.controller.dto;

import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants
public record ThemeWebResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public ThemeWebResponse {
        validate(id, name, description, thumbnail);
    }

    private static void validate(final Long id, final String name, final String description, final String thumbnail) {
        Validator.of(ThemeWebResponse.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.name, name)
                .notNullField(Fields.description, description)
                .notNullField(Fields.thumbnail, thumbnail);
    }
}
