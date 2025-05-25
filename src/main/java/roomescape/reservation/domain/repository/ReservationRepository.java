package roomescape.reservation.domain.repository;

import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findByReservationMemberId(Long id);

    void deleteByReservationSlotIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByReservationSlotIdAndMemberId(Long reservationId, Long memberId);

    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);

    void deleteById(Long waitingId);
}
