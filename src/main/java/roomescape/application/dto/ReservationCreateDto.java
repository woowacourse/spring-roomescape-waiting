package roomescape.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

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
    public static ReservationCreateDto of(UserReservationCreateDto dto, Long memberId) {
        return new ReservationCreateDto(dto.date(), dto.themeId(), dto.timeId(), memberId);
    }
}
