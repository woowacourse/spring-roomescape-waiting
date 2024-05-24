package roomescape.controller.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Status;
import roomescape.service.dto.response.MyReservationResponse;

public record MyReservationWebResponse(
        @NotNull
        Long reservationId,
        @NotBlank
        String theme,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        @NotBlank
        String status
) {
    public static MyReservationWebResponse from(MyReservationResponse response) {
        return new MyReservationWebResponse(
                response.reservationId(),
                response.theme(),
                response.date(),
                response.time(),
                convertToStatusMessage(response.status(), response.rank())
        );
    }

    private static String convertToStatusMessage(Status status, Long rank) {
        if (status == Status.CREATED) {
            return "예약";
        }

        if (status == Status.WAITING) {
            return String.format("%d번째 대기", rank);
        }

        return "";
    }
}
