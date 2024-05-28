package roomescape.service.dto.output;

import java.util.List;
import java.util.stream.Stream;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.repository.dto.WaitingWithRank;

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
                "예약"
        );
    }

    private static ReservationOutput toOutput(final WaitingWithRank waitingWithRank) {
        final Waiting waiting = waitingWithRank.getWaiting();
        return new ReservationOutput(
                waiting.getId(),
                ThemeOutput.toOutput(waiting.getTheme()),
                waiting.getDate()
                        .asString(),
                ReservationTimeOutput.toOutput(waiting.getTime()),
                MemberOutput.toOutput(waiting.getMember()),
                waitingWithRank.getRank() + "번째 예약대기"
        );
    }

    public static List<ReservationOutput> toOutputs(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationOutput::toOutput)
                .toList();
    }

    public static List<ReservationOutput> toOutputs(final List<Reservation> reservations,
                                                    final List<WaitingWithRank> waitings) {
        final List<ReservationOutput> waitingOutputs = waitings.stream()
                .map(ReservationOutput::toOutput)
                .toList();
        final List<ReservationOutput> outputs = toOutputs(reservations);
        return Stream.concat(waitingOutputs.stream(), outputs.stream())
                .toList();
    }
}
