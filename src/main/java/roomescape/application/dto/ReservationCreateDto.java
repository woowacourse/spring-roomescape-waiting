package roomescape.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.presentation.controller.dto.UserReservationRequest;

public record ReservationCreateDto(
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

    public static ReservationCreateDto of(UserReservationRequest dto, Long memberId) {
        return new ReservationCreateDto(dto.date(), dto.themeId(), dto.timeId(), memberId);
    }
}
