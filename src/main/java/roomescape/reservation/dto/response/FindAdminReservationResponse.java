package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.Reservation;

public record FindAdminReservationResponse(Long id,
                                           String member,
                                           String theme,
                                           LocalDate date,
                                           LocalTime startAt) {
    public static FindAdminReservationResponse from(final Reservation reservation) {
        return new FindAdminReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt()
        );
    }
}
