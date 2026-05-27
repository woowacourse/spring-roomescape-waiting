package roomescape.reservation.service;

import java.util.List;

import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponseDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.dto.ReservationSaveServiceDto;

public interface ReservationService {
    List<Reservation> getAll();
    Reservation create(ReservationSaveServiceDto reservation);
    void cancel(Long id);
    List<Reservation> getByName(String name);
    void cancelForUser(Long id, String name);
    Reservation update(Long id, Long timeId);
    List<ReservationWithWaitingOrderResponseDto> getAllByName(String name);
}
