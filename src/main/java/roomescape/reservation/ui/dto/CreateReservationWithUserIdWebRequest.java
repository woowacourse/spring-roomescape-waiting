package roomescape.reservation.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.user.domain.UserId;

import java.time.LocalDate;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationWithUserIdWebRequest(LocalDate date,
                                                    Long timeId,
                                                    Long themeId,
                                                    Long userId) {

    public CreateReservationWithUserIdWebRequest {
        validate(date, timeId, themeId, userId);
    }

    public CreateReservationRequest toServiceRequest() {
        return new CreateReservationRequest(
                UserId.from(userId),
                ReservationDate.from(date),
                TimeSlotId.from(timeId),
                ThemeId.from(themeId));
    }

    private void validate(final LocalDate date, final Long timeId, final Long themeId, final Long userId) {
        Validator.of(CreateReservationWithUserIdWebRequest.class)
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.timeId, timeId, DomainTerm.TIME_SLOT_ID.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label())
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label());
    }
}
