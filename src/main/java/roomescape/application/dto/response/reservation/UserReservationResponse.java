package roomescape.application.dto.response.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;

public record UserReservationResponse(
        @NotNull(message = "예약 아이디는 빈값이 올 수 없습니다.") Long reservationId,
        @NotBlank(message = "예약 테마는 빈값이 올 수 없습니다.") String theme,
        @NotNull(message = "예약 일자는 빈값이 올 수 없습니다.") LocalDate date,
        @NotNull(message = "예약 시간은 빈값이 올 수 없습니다.") LocalTime time,
        @NotBlank(message = "예약 상태는 빈값이 올 수 없습니다.") String status
) {
    public static UserReservationResponse of(Reservation reservation, Long rank) {
        if (!reservation.isReserved()) {
            rank++;
        }
        if (rank == 0) {
            return new UserReservationResponse(
                    reservation.getId(),
                    reservation.getDetail().getTheme().getName(),
                    reservation.getDetail().getDate(),
                    reservation.getDetail().getTime().getStartAt(), "예약");
        }
        return new UserReservationResponse(
                reservation.getDetail().getId(),
                reservation.getDetail().getTheme().getName(),
                reservation.getDetail().getDate(),
                reservation.getDetail().getTime().getStartAt(), rank + "번째 예약");
    }
}
