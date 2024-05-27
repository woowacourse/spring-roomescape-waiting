package roomescape.reservation.dto;

import roomescape.member.dto.MemberNameResponse;
import roomescape.reservation.domain.*;
import roomescape.theme.dto.ThemeResponse;

import java.time.format.DateTimeFormatter;

public record ReservationResponse(
//        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long id,
        String name,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberNameResponse member,
        String status
) {

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getMember().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme()),
                new MemberNameResponse(reservation.getMember()),
                ReservationStatus.RESERVED.getPrintName()
        );
    }

    public ReservationResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName().name(),
                waiting.getDate(DateTimeFormatter.ISO_DATE),
                new ReservationTimeResponse(waiting.getReservationTime()),
                new ThemeResponse(waiting.getTheme()),
                new MemberNameResponse(waiting.getMember()),
                null
        );
    }

    public static ReservationResponse fromWaitingWithRank(WaitingWithRank waitingWithRank) {
        String reservationStatus = decideReservationStatus(waitingWithRank.rank());
        Waiting waiting = waitingWithRank.waiting();
        return new ReservationResponse(
                waiting.getId(),
                waiting.getMember().getName().name(),
                waiting.getDate(DateTimeFormatter.ISO_DATE),
                new ReservationTimeResponse(waiting.getReservationTime()),
                new ThemeResponse(waiting.getTheme()),
                new MemberNameResponse(waiting.getMember()),
                reservationStatus
        );
    }

    private static String decideReservationStatus(Rank rank) {
        return rank.getWaitingCount() + "번째 " + ReservationStatus.WAITING.getPrintName();
    }
}
