package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithRank;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByNameAndTheme_IdAndTime(String name, Long themeId, ReservationTime time);

    boolean existsByTheme_IdAndTime(Long themeId, ReservationTime time);

    List<Reservation> findAllByTime_IdAndTheme_Id(Long timeId, Long themeId);

    List<Reservation> findAllByTime_IdAndTheme_IdAndStatus(Long timeId, Long themeId, Status status);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findAllByStatus(Status status);

    @EntityGraph(attributePaths = {"theme","time"})
    List<Reservation> findByName(String name);

    @Query("SELECT r.time.id FROM Reservation r " +
            "WHERE r.theme.id = :themeId " +
            "AND r.time.startAt >= :start AND r.time.startAt < :end")
    List<Long> findAvailableTimeIds(Long themeId, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT new roomescape.reservation.repository.dto.ReservationWithRank(
                r,
                (SELECT COUNT(w) FROM Reservation w
                 WHERE w.theme = r.theme
                   AND w.time = r.time
                   AND w.status = roomescape.reservation.domain.Status.WAITING
                   AND w.createdAt < r.createdAt))
            FROM Reservation r
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE r.name = :name
            ORDER BY r.id
            """)
    List<ReservationWithRank> findMineWithRank(String name);
}
