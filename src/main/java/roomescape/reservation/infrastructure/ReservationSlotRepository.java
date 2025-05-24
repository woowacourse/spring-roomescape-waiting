package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationSlot;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    @EntityGraph(attributePaths = {"allReservations"})
    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @EntityGraph(attributePaths = {"time"})
    List<ReservationSlot> findAllByDateAndThemeId(LocalDate date, Long themeId);

    @EntityGraph(attributePaths = {
            "allReservations",
            "allReservations.member",
            "time",
            "theme"
    })
    List<ReservationSlot> findAll();
}
