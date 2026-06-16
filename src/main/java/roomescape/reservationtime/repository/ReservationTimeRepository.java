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

    @Query(value = """
            SELECT rt.*
            FROM reservation_time rt
            LEFT JOIN reservation r ON rt.id = r.time_id AND r.date = :date AND r.theme_id = :themeId
            WHERE r.id IS NULL
            ORDER BY rt.start_at ASC
            """, nativeQuery = true)
    List<ReservationTime> findAvailableByDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") Long themeId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = :timeId)", nativeQuery = true)
    boolean existsReservationByTimeId(@Param("timeId") Long timeId);
}