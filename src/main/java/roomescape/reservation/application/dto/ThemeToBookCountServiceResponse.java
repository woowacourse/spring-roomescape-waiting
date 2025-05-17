package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.BookedCount;
import roomescape.theme.domain.Theme;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ThemeToBookCountServiceResponse(
        Theme theme,
        BookedCount bookedCount
) {

    public ThemeToBookCountServiceResponse {
        validate(theme, bookedCount);
    }

    private void validate(final Theme theme, final BookedCount bookedCount) {
        Validator.of(ThemeToBookCountServiceResponse.class)
                .validateNotNull(Fields.theme, theme, DomainTerm.THEME_ID.label())
                .validateNotNull(Fields.bookedCount, bookedCount, DomainTerm.BOOKED_COUNT.label());
    }
}
