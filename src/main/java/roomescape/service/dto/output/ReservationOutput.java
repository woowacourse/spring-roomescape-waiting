package roomescape.service.dto.output;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationInfo;

import java.util.List;

public record ReservationOutput(long id, ThemeOutput theme, String date, ReservationTimeOutput time,
                                MemberOutput member) {

    public static ReservationOutput toOutput(final Reservation reservation) {
        final ReservationInfo reservationInfo = reservation.getReservationInfo();
        return new ReservationOutput(
                reservation.getId(),
                ThemeOutput.toOutput(reservationInfo.getTheme()),
                reservationInfo.getDate()
                        .asString(),
                ReservationTimeOutput.toOutput(reservationInfo.getTime()),
                MemberOutput.toOutput(reservation.getMember())
        );
    }

    public static List<ReservationOutput> toOutputs(final List<Reservation> reservationInfos) {
        return reservationInfos.stream()
                .map(ReservationOutput::toOutput)
                .toList();
    }
}
