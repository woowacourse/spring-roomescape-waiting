package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record ReservationChangeCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public ReservationCreateCommand toCreateCommand() {
        return ReservationCreateCommand.builder()
                .name(name)
                .date(date)
                .timeId(timeId)
                .themeId(themeId)
                .build();
    }
}
