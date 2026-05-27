package roomescape.reservation.service;

import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponseDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.dto.ReservationSaveServiceDto;

import java.util.List;

public interface ReservationService {
    List<Reservation> getAll();
    Reservation create(ReservationSaveServiceDto reservation);
    void cancel(Long id);
    void cancelForUser(Long id, String name);
    Reservation update(Long id, Long timeId);
    List<ReservationWithWaitingOrderResponseDto> getAllByName(String name);
}
