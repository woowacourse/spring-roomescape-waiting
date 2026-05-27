package roomescape.dto;

import java.util.List;

public record UserBookingResponseDTO(
        List<ReservationResponseDTO> reservations,
        List<WaitingResponseDTO> waitings
) {

    public static UserBookingResponseDTO of(
            List<ReservationResponseDTO> reservatoins,
            List<WaitingResponseDTO> waitings
    ) {
        return new UserBookingResponseDTO(
                reservatoins,
                waitings
        );
    }
}
