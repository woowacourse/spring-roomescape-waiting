package roomescape.reservation.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;
import roomescape.time.domain.ReservationTimeId;
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

    public CreateReservationServiceRequest toServiceRequest() {
        return new CreateReservationServiceRequest(
                UserId.from(userId),
                ReservationDate.from(date),
                ReservationTimeId.from(timeId),
                ThemeId.from(themeId));
    }

    private void validate(final LocalDate date, final Long timeId, final Long themeId, final Long userId) {
        Validator.of(CreateReservationWithUserIdWebRequest.class)
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.timeId, timeId, DomainTerm.RESERVATION_TIME_ID.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label())
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label());
    }
}
