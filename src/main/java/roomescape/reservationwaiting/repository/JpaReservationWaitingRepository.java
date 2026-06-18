package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationwaiting.ReservationWaiting;

public interface JpaReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteByIdAndName(Long id, String name);

    boolean existsByIdAndName(Long id, String name);

    boolean existsByDateAndThemeIdAndTimeIdAndName(LocalDate date, Long themeId, Long timeId, String name);

    Optional<ReservationWaiting> findFirstByDateAndThemeIdAndTimeIdOrderByRequestAtAsc(LocalDate date, Long themeId, Long timeId);
}
