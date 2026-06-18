package roomescape.reservation.repository.jpa;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.repository.entity.ReservationEntity;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

    @EntityGraph(attributePaths = {"time", "theme"})
    List<ReservationEntity> findAll();

    Optional<ReservationEntity> findById(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    @EntityGraph(attributePaths = {"time", "theme"})
    @Query("SELECT r FROM ReservationEntity r WHERE r.customerName = :customerName AND (r.date > :date OR (r.date = :date AND r.time.startAt > :time))")
    List<ReservationEntity> findByCustomerNameAfter(
        @Param("customerName") String customerName,
        @Param("date") LocalDate date,
        @Param("time") LocalTime time
    );

    @Query(nativeQuery = true, value = """
        SELECT rt.id, rt.start_at AS startAt,
               CASE WHEN r.id IS NOT NULL THEN TRUE ELSE FALSE END AS reserved
        FROM reservation_time rt
        LEFT JOIN reservation r
            ON r.time_id = rt.id
           AND r.date = :date
           AND r.theme_id = :themeId
        ORDER BY rt.start_at
        """)
    List<ReservationTimeStatusProjection> findTimeStatuses(
        @Param("date") LocalDate date,
        @Param("themeId") Long themeId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReservationEntity r WHERE r.date = :date AND r.timeId = :timeId AND r.themeId = :themeId")
    Optional<ReservationEntity> findBySlotForUpdate(
        @Param("date") LocalDate date,
        @Param("timeId") long timeId,
        @Param("themeId") long themeId
    );
}
