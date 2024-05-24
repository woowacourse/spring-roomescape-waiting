package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(Long reservationId,
                                        String theme,
                                        @JsonFormat(pattern = "YYYY-MM-dd") LocalDate date,
                                        @JsonFormat(pattern = "HH:mm") LocalTime time,
                                        String status) {

    public MemberReservationResponse(ReservationWaitingWithRank reservationWaitingWithRank) {
        this(
                reservationWaitingWithRank.getReservation().getId(),
                reservationWaitingWithRank.getReservation().getTheme().getName(),
                reservationWaitingWithRank.getReservation().getDate(),
                reservationWaitingWithRank.getReservation().getReservationTime().getStartAt(),
                reservationStatusToString(reservationWaitingWithRank)
        );
    }

    private static String reservationStatusToString(ReservationWaitingWithRank reservationWaitingWithRank) {
        ReservationStatus reservationStatus = reservationWaitingWithRank.getReservation().getReservationStatus();
        if (reservationStatus.isReserved()) {
            return reservationStatus.getStatus();
        }
        return reservationWaitingWithRank.getRank() + "번째 " + reservationStatus.getStatus();
    }
}
