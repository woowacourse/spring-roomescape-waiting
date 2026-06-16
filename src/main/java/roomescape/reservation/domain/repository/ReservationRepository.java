package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;

public interface ReservationRepository {
    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    Integer update(Long id, ReservationSlot slot);

    Integer updateStatus(Long id, ReservationStatus status);

    Integer delete(Long id);

    Boolean existsBySlot(ReservationSlot slot);

    Boolean existsByUserAndSlot(String username, ReservationSlot slot);

    Boolean existsByTheme(Long themeId);

    Boolean existsByTime(Long timeId);

    Optional<ReservationSlot> findSlotById(Long id);
}
