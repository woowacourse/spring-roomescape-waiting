package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findByTimeId(long timeId);

    List<Reservation> findByThemeId(long themeId);

    List<Reservation> findByMemberId(long memberId);

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(
            LocalDate date, long timeId, long themeId, Status status
    );

    Optional<Reservation> findByDateAndTimeIdAndThemeIdAndMemberId(
            LocalDate date, long timeId, long themeId, long memberId
    );

    List<Reservation> findByStatus(Status status);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.date = :date
                AND r.time.id = :timeId
                AND r.theme.id = :themeId
                AND r.status = :status
            ORDER BY r.id ASC
            LIMIT 1
            """)
    Optional<Reservation> findEarliestRegisteredWaiting(
            @Param("date") LocalDate date, @Param("timeId") long timeId,
            @Param("themeId") long themeId, @Param("status") Status status
    );

    @Modifying
    @Query("delete from Reservation where id = :id")
    int deleteById(@Param("id") long id);
}
