package roomescape.service.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.reservation.ReservationWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId,
        String theme,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul") LocalTime time,
        ReservationStatusResponse reservationStatus
) {
    public static MemberReservationResponse from(ReservationWithRank reservationWithRank) {
        return new MemberReservationResponse(
                reservationWithRank.getReservation().getId(),
                reservationWithRank.getReservation().getTheme().getName().getValue(),
                reservationWithRank.getReservation().getDate(),
                reservationWithRank.getReservation().getTime(),
                new ReservationStatusResponse(reservationWithRank.getReservation().getStatus().getDescription(), reservationWithRank.getRank())
        );
    }
}
