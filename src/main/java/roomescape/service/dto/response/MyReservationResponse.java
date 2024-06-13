package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationWithRank;
import roomescape.domain.Status;

public record MyReservationResponse(
        @NotNull
        Long id,
        @NotBlank
        String theme,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        @NotBlank
        Status status,
        @NotNull
        Long rank)
{
    public static MyReservationResponse from(ReservationWithRank reservationWithRank) {
        return new MyReservationResponse(
                reservationWithRank.getReservation().getId(),
                reservationWithRank.getReservation().getTheme().getName(),
                reservationWithRank.getReservation().getDate(),
                reservationWithRank.getReservation().getTime().getStartAt(),
                reservationWithRank.getReservation().getStatus(),
                reservationWithRank.getRank());
    }
}
