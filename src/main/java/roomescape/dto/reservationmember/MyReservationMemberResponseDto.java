package roomescape.dto.reservationmember;

import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.domain.waiting.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationMemberResponseDto(Long id, String name, String themeName,
                                             LocalDate date, LocalTime startAt, String status) {

    public static MyReservationMemberResponseDto from(ReserveTicket reserveTicket) {
        return new MyReservationMemberResponseDto(reserveTicket.getReservationId(), reserveTicket.getName(), reserveTicket.getThemeName(),
                reserveTicket.getDate(), reserveTicket.getStartAt(), "Reserved");
    }

    public static MyReservationMemberResponseDto from(WaitingWithRank waitingWithRank) {
        return new MyReservationMemberResponseDto(waitingWithRank.getWaitingId(), waitingWithRank.getName(), waitingWithRank.getThemeName(),
                waitingWithRank.getDate(), waitingWithRank.getStartAt(), String.format("%dth waiting", waitingWithRank.getRank()));
    }
}
