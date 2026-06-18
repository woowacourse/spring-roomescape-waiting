package roomescape.reservation.service;

import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithRank;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;

import java.util.List;

public interface ReservationService {
    List<Reservation> getAll();
    Reservation create(ReservationSaveServiceRequest reservation);
    void cancel(Long id);
    void cancelForUser(Long id, String name);
    Reservation update(Long id, Long timeId, String name);
    List<ReservationWithWaitingOrderResponse> getAllByName(String name);

    List<ReservationResponse> findMine(String name);
    List<ReservationWithRank> findMineWithRank(String name);

    List<ReservationResponse> getWaitings();

    Reservation requestWaiting(ReservationSaveServiceRequest request);

    void cancelWaiting(Long id, String name);
}
