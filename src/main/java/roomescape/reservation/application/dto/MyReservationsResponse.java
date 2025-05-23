package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.theme.ui.dto.ThemeResponse;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record MyReservationsResponse(Long id,
                                     LocalDate date,
                                     ReservationTime time,
                                     ThemeResponse theme,
                                     int sequence) {

    public MyReservationsResponse {
        validate(id, date, time, theme, sequence);
    }

    public static MyReservationsResponse fromReservation(final Reservation domain) {
        return new MyReservationsResponse(
                domain.getId(),
                domain.getDate().getValue(),
                domain.getTime(),
                ThemeResponse.from(domain.getTheme()),
                0
        );
    }

    public static MyReservationsResponse fromWaiting(final WaitingReservation domain) {
        return new MyReservationsResponse(
                domain.getId(),
                domain.getDate().getValue(),
                domain.getTime(),
                ThemeResponse.from(domain.getTheme()),
                domain.getWaitingOrder()
        );
    }

    private void validate(final Long id,
                          final LocalDate date,
                          final ReservationTime time,
                          final ThemeResponse themeResponse,
                          final int sequence) {
        Validator.of(MyReservationsResponse.class)
                .validateNotNull(Fields.id, id, DomainTerm.RESERVATION_WAITING_Id.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.theme, themeResponse, DomainTerm.THEME.label())
                .validateNotNull(Fields.sequence, sequence, DomainTerm.RESERVATION_WAITING_ORDER.label());
    }
}
