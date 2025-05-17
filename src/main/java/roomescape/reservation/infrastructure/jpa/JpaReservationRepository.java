package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationCustomRepository {

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeStartAtAndThemeId(LocalDate date, LocalTime time, Long themeId);

    boolean existsByThemeId(Long themeId);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findByMemberId(Long id);

    @EntityGraph(attributePaths = {"theme", "member", "time"})
    List<Reservation> findAll();
}
