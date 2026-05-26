package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {
    Optional<Reservation> findById(Long id);

    Reservation save(Reservation reservation);

    Integer update(Reservation reservation);

    Integer delete(Long id);

    Boolean existsDuplicate(Reservation reservation);

    Boolean existsDuplicateExcluding(Reservation reservation);

    Boolean existsByTheme(Long themeId);

    Boolean existsByTime(Long timeId);
}
