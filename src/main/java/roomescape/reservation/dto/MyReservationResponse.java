package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyReservationResponse of(Reservation reservation, Status status, int rank) {
        if (status == Status.CONFIRMED) {
            return new MyReservationResponse(
                    reservation.getId(),
                    reservation.getReservationContent().getTheme()
                            .getName(),
                    reservation.getReservationContent().getDate(),
                    reservation.getReservationContent().getTime()
                            .getStartAt(),
                    "예약 확정"
            );
        }
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getReservationContent().getTheme()
                        .getName(),
                reservation.getReservationContent().getDate(),
                reservation.getReservationContent().getTime()
                        .getStartAt(),
                "대기 번호 %d번".formatted(rank)
        );


    }

}
