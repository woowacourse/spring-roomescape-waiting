package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.Status;

@Builder
public record ReservationChangeCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId,
        Status status
) {
    public ReservationCreateCommand toCreateCommand() {
        return ReservationCreateCommand.builder()
                .name(name)
                .date(date)
                .timeId(timeId)
                .themeId(themeId)
                .build();
    }

    public ReservationCancelCommand toCancelCommand() {
        return ReservationCancelCommand.builder()
                .name(name)
                .status(status)
                .build();
    }
}
