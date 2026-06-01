package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public interface ReservationRepository {
    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    Integer update(Long id, ReservationSlot slot);

    Integer delete(Long id);

    Boolean existsBySlot(ReservationSlot slot);

    Boolean existsDuplicateExcluding(Reservation reservation);

    Boolean existsByTheme(Long themeId);

    Boolean existsByTime(Long timeId);

    Optional<ReservationSlot> findSlotById(Long id);
}
