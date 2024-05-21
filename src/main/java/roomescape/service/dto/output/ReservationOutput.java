package roomescape.service.dto.output;

import roomescape.domain.reservation.ReservationInfo;

import java.util.List;

public record ReservationOutput(long id, ThemeOutput theme, String date, ReservationTimeOutput time,
                                MemberOutput member) {

    public static ReservationOutput toOutput(final ReservationInfo reservationInfo) {
        return new ReservationOutput(
                reservationInfo.getId(),
                ThemeOutput.toOutput(reservationInfo.getTheme()),
                reservationInfo.getDate()
                        .asString(),
                ReservationTimeOutput.toOutput(reservationInfo.getTime()),
                MemberOutput.toOutput(reservationInfo.getMember())
        );
    }

    public static List<ReservationOutput> toOutputs(final List<ReservationInfo> reservationInfos) {
        return reservationInfos.stream()
                .map(ReservationOutput::toOutput)
                .toList();
    }
}
