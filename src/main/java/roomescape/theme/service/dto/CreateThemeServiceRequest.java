package roomescape.theme.service.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateThemeServiceRequest(
        String name,
        String description,
        String thumbnail
) {

    public CreateThemeServiceRequest {
        validate(name, description, thumbnail);
    }

    private void validate(final String name, final String description, final String thumbnail) {
        Validator.of(CreateThemeServiceRequest.class)
                .notNullField(CreateThemeServiceRequest.Fields.name, name)
                .notNullField(CreateThemeServiceRequest.Fields.description, description)
                .notNullField(CreateThemeServiceRequest.Fields.thumbnail, thumbnail);
    }
}
