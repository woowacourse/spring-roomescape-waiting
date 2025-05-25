package roomescape.reservation.domain.repository;

import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findByWaitingMemberId(Long id);

    void deleteByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    List<Reservation> findAllByWaitingStatus(ReservationStatus reservationStatus);

    void deleteById(Long waitingId);
}
