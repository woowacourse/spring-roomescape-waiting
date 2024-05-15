package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);
}
