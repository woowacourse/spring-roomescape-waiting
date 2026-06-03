package roomescape.feature.reservation.service;

import java.util.List;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;

public interface AdminReservationService {

    List<ReservationResponseDto> getReservations();

    void deleteReservationById(Long id);
}
