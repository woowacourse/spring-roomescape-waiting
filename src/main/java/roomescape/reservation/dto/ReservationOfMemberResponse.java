package roomescape.reservation.dto;

import roomescape.reservation.domain.Rank;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingWithRank;

import java.time.format.DateTimeFormatter;

public record ReservationOfMemberResponse(Long id, String themeName, String date, String reservationTime, String status) {

    public static ReservationOfMemberResponse from(Reservation reservation) {
        return new ReservationOfMemberResponse(
                reservation.getId(),
                reservation.getTheme().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                reservation.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                ReservationStatus.RESERVED.getPrintName()
        );
    }

    public static ReservationOfMemberResponse from(WaitingWithRank waitingWithRank) {
        String reservationStatus = decideReservationStatus(waitingWithRank.getRank());
        return new ReservationOfMemberResponse(
                waitingWithRank.getWaiting().getId(),
                waitingWithRank.getWaiting().getTheme().getName().name(),
                waitingWithRank.getWaiting().getDate(DateTimeFormatter.ISO_DATE),
                waitingWithRank.getWaiting().getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                reservationStatus
        );
    }

    private static String decideReservationStatus(Rank rank) {
        return rank.getWaitingCount() + ReservationStatus.WAITING.getPrintName();
    }
}
