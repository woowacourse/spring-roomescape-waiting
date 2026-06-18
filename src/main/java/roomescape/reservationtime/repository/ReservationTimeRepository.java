package roomescape.reservationtime.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservationtime.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Override
    @Query("SELECT t FROM ReservationTime t ORDER BY t.startAt ASC")
    List<ReservationTime> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ReservationTime t WHERE t.id = :id")
    void lockById(@Param("id") Long id);

    @Query("""
            SELECT t FROM ReservationTime t
            WHERE t.id NOT IN (
                SELECT r.time.id FROM Reservation r
                WHERE r.date = :date AND r.theme.id = :themeId)
            ORDER BY t.startAt ASC
            """)
    List<ReservationTime> findAvailableByDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") Long themeId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.time.id = :timeId")
    boolean existsReservationByTimeId(@Param("timeId") Long timeId);
}