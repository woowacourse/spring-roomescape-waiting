package roomescape.dto;

import roomescape.domain.ReservationTime;

public record ReservationTimeResponseDTO(
        Long id,
        String startAt

) {

    public static ReservationTimeResponseDTO from(ReservationTime reservationTime) {
        return new ReservationTimeResponseDTO(
                reservationTime.getId(),
                reservationTime.getStartAt().toString()
        );
    }
}
