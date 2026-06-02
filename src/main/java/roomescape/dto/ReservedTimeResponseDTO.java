package roomescape.dto;

import java.time.LocalTime;

public record ReservedTimeResponseDTO(
        Long timeId,
        LocalTime startAt,
        boolean reserved
) {
    public static ReservedTimeResponseDTO create(Long timeId, LocalTime startAt, Long reservationId) {
        return new ReservedTimeResponseDTO(
                timeId,
                startAt,
                reservationId != null
        );
    }
}
