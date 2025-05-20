package roomescape.reservation.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.time.domain.ReservationTimeId;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.time.LocalDate;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationWebRequest(LocalDate date,
                                          Long timeId,
                                          Long themeId) {

    public CreateReservationWebRequest {
        validate(date, timeId, themeId);
    }

    public CreateReservationRequest toRequestWithUserId(final UserId userId) {
        return new CreateReservationRequest(
                userId,
                ReservationDate.from(date),
                ReservationTimeId.from(timeId),
                ThemeId.from(themeId)
        );
    }

    private void validate(final LocalDate date, final Long timeId, final Long themeId) {
        Validator.of(CreateReservationWithUserIdWebRequest.class)
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.timeId, timeId, DomainTerm.RESERVATION_TIME_ID.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label());
    }
}
