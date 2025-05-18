package roomescape.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.presentation.controller.dto.UserReservationCreateRequest;

public record ReservationCreateServiceRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate date,
        @NotNull
        Long themeId,
        @NotNull
        Long timeId,
        @NotNull
        Long memberId
) {

    public static ReservationCreateServiceRequest of(UserReservationCreateRequest dto, Long memberId) {
        return new ReservationCreateServiceRequest(dto.date(), dto.themeId(), dto.timeId(), memberId);
    }
}
