package roomescape.service.dto.output;

import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.Waiting;

public record WaitingOutput(long id, ThemeOutput theme, String date, ReservationTimeOutput time,
                            MemberOutput member, int order) {

    public static WaitingOutput toOutput(final Waiting waiting, final int order) {
        final ReservationInfo reservationInfo = waiting.getReservationInfo();
        return new WaitingOutput(
                waiting.getId(),
                ThemeOutput.toOutput(reservationInfo.getTheme()),
                reservationInfo.getDate()
                        .asString(),
                ReservationTimeOutput.toOutput(reservationInfo.getTime()),
                MemberOutput.toOutput(waiting.getMember()),
                order
        );
    }
}
