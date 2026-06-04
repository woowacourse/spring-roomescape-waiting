package roomescape.reservation.domain;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.domain.dto.ReservationQueryResult;

public interface PendingReservationRepository {
    PendingReservation save(PendingReservation pendingReservation);
    PendingReservation insertWithId(PendingReservation pendingReservation);
    Optional<PendingReservation> findNextPendingReservation(Long slotId);
    Optional<PendingReservation> findById(Long id);
    boolean existsReservationByName(Long slotId, String name);
    boolean existsById(Long id);
    void cancel(PendingReservation reservation);
    void update(PendingReservation reservation);
    List<PendingReservation> findAll();
    List<ReservationQueryResult> findAllByName(String name);

    default PendingReservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("대기중인 예약이 없습니다."));
    }
}
