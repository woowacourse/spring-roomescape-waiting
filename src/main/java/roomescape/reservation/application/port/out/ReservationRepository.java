package roomescape.reservation.application.port.out;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<ReservationDetailProjection> findAll();

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId);

    void deleteById(long reservationId);

    Optional<ReservationDetailProjection> findDetailById(long reservationId);

    Optional<Reservation> findById(long reservationId);

    boolean existsBySlotId(long slotId);

    boolean existsByMemberIdAndSlotId(long memberId, long slotId);
}
