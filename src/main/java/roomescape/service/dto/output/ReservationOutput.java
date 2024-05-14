package roomescape.service.dto.output;

import java.util.List;
import java.util.Set;

import roomescape.domain.reservation.Reservation;

public record ReservationOutput(long id, ThemeOutput theme, String date, ReservationTimeOutput time,
                                MemberOutput member) {

    public static ReservationOutput toOutput(final Reservation reservation) {
        return new ReservationOutput(
                reservation.getId(),
                ThemeOutput.toOutput(reservation.getTheme()),
                reservation.getDate().asString(),
                ReservationTimeOutput.toOutput(reservation.getTime()),
                MemberOutput.toOutput(reservation.getMember())
        );
    }

    public static List<ReservationOutput> toOutputs(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationOutput::toOutput)
                .toList();
    }
    public static List<ReservationOutput> toOutputs(final Set<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationOutput::toOutput)
                .toList();
    }
}
