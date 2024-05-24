package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.WaitingWithRank;

public record PersonalReservationResponse(
        Long id,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String theme,
        String status
) {

    public static PersonalReservationResponse from(Reservation reservation) {
        return new PersonalReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getRawName(),
                "예약"
        );
    }

    public static PersonalReservationResponse from(WaitingWithRank waitingWithRank) {
        return new PersonalReservationResponse(
                waitingWithRank.id(),
                waitingWithRank.date(),
                waitingWithRank.time(),
                waitingWithRank.themeName(),
                String.format("%d번째 예약 대기", waitingWithRank.rank())
        );
    }
}
