package roomescape.reservation.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    boolean confirmPayment(long reservationId, String orderId, String paymentKey);

    List<ReservationDetailProjection> findAll();

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId);

    void deleteById(long reservationId);

    Optional<Reservation> findById(long reservationId);

    Optional<Reservation> findByOrderId(String orderId);

    boolean existsBySlotId(long slotId);

    boolean existsByMemberIdAndSlotId(long memberId, long slotId);

    void deletePendingByOrderIdAndMemberId(String orderId, long memberId);
}
