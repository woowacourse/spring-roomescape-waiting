package roomescape.service.dto.output;

import roomescape.domain.reservation.Reservation;

import java.util.List;

public record ReservationOutput(long id, ThemeOutput theme, String date, ReservationTimeOutput time,
                                MemberOutput member, String reservationStatus) {

    public static ReservationOutput toOutput(final Reservation reservation) {
        return new ReservationOutput(
                reservation.getId(),
                ThemeOutput.toOutput(reservation.getTheme()),
                reservation.getDate()
                        .asString(),
                ReservationTimeOutput.toOutput(reservation.getTime()),
                MemberOutput.toOutput(reservation.getMember()),
                reservation.getReservationStatus()
                        .getValue()
        );
    }

    public static List<ReservationOutput> toOutputs(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationOutput::toOutput)
                .toList();
    }
}
