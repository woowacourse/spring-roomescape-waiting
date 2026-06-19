package roomescape.reservation.service;

import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;

import java.util.List;

public interface ReservationService {
    List<Reservation> getAll();

    Reservation create(ReservationSaveServiceRequest reservation);

    void cancel(Long id);

    void cancelByOrderId(String orderId);

    void cancelForUser(Long id, String name);

    Reservation update(Long id, Long timeId);

    List<ReservationWithWaitingOrderResponse> getAllByName(String name);
}
