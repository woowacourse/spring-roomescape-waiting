package roomescape.reservation.service.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;
import roomescape.theme.domain.Theme;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ThemeToBookCountServiceResponse(Theme theme) {

    public ThemeToBookCountServiceResponse {
        validate(theme);
    }

    private void validate(final Theme theme) {
        Validator.of(ThemeToBookCountServiceResponse.class)
                .notNullField(Fields.theme, theme);
    }
}
