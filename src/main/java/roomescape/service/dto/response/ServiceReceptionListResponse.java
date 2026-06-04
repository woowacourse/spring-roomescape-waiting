package roomescape.service.dto.response;

import java.util.List;
import roomescape.domain.Reservation;
import roomescape.repository.dto.WaitDetailDto;

public record ServiceReceptionListResponse(
        List<ServiceReservationResponse> reservations,
        List<ServiceWaitResponse> waits
) {
    public static ServiceReceptionListResponse from(List<Reservation> reservations, List<WaitDetailDto> waits) {
        List<ServiceReservationResponse> reservationResponses = reservations.stream()
                .map(ServiceReservationResponse::from)
                .toList();

        List<ServiceWaitResponse> waitResponses = waits.stream()
                .map(ServiceWaitResponse::from)
                .toList();

        return new ServiceReceptionListResponse(reservationResponses, waitResponses);
    }
}
