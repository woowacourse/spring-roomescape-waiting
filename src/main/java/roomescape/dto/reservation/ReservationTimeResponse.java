package roomescape.dto.reservation;

import roomescape.domain.reservation.ReservationTime;

import java.time.format.DateTimeFormatter;

public record ReservationTimeResponse(
        Long id,
        String startAt
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        final String startAt = reservationTime.getStartAt().format(FORMATTER);
        return new ReservationTimeResponse(reservationTime.getId(), startAt);
    }
}
