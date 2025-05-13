package roomescape.reservation.service.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;
import roomescape.theme.domain.Theme;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ThemeToBookCountServiceResponse(
        Theme theme,
        int bookedCount
) {

    public ThemeToBookCountServiceResponse {
        validate(theme, bookedCount);
    }

    private void validate(final Theme theme, final int bookedCount) {
        Validator.of(ThemeToBookCountServiceResponse.class)
                .notNullField(Fields.theme, theme)
                .notNullField(Fields.bookedCount, bookedCount);
    }
}
