package roomescape.reservation.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    @Modifying
    @Query(value = """
        MERGE INTO reservation_slot (date_id, time_id, theme_id)
        KEY(date_id, time_id, theme_id)
        VALUES (:#{#date.id}, :#{#time.id}, :#{#theme.id})
        """, nativeQuery = true)
    void saveIfAbsent(
        @Param("date") ReservationDate date,
        @Param("time") ReservationTime time,
        @Param("theme") Theme theme);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT rs
        FROM reservation_slot rs
        WHERE rs.date = :date
        AND rs.time = :time
        AND rs.theme = :theme
        """)
    Optional<ReservationSlot> findByDateAndTimeAndThemeForUpdate(
        @Param("date") ReservationDate date,
        @Param("time") ReservationTime time,
        @Param("theme") Theme theme);
}
