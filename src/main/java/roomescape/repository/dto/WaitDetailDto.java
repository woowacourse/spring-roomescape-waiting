package roomescape.repository.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Wait;

public record WaitDetailDto(
        Long id,
        LocalDateTime createdAt,
        String name,
        LocalDate reservationDate,
        ReservationTimeDto reservationTime,
        ThemeDto theme,
        Long order
) {
    public static WaitDetailDto from(Wait wait, Long order) {
        return new WaitDetailDto(
                wait.getId(),
                wait.getCreatedAt(),
                wait.getName(),
                wait.getReservationDate(),
                ReservationTimeDto.from(wait.getTime()),
                ThemeDto.from(wait.getTheme()),
                order
        );
    }

    public Wait toEntity() {
        return new Wait(
                id,
                createdAt,
                name,
                reservationDate,
                reservationTime.toEntity(),
                theme.toEntity()
        );
    }
}
