package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record ReservationWaitingResponse(
        @NotNull
        Long id,
        @NotBlank
        String name,
        @NotBlank
        String theme,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt) {

    public static ReservationWaitingResponse from(Reservation reservation) {
        return new ReservationWaitingResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt());
    }
}

