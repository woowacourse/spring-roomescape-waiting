package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, ReservationCustomRepository {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.time WHERE r.date = :date AND r.theme.id = :themeId")
    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, Long timeId, Long themeId,
                                                     ReservationStatus status);

    boolean existsByThemeId(Long themeId);

    @EntityGraph(attributePaths = {"theme", "member", "time"})
    List<Reservation> findAll();
}
