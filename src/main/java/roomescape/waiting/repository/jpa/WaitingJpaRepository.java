package roomescape.waiting.repository.jpa;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.waiting.repository.entity.WaitingEntity;

public interface WaitingJpaRepository extends JpaRepository<WaitingEntity, Long> {

    @Query("""
        SELECT w FROM WaitingEntity w
        JOIN FETCH w.time
        JOIN FETCH w.theme
        WHERE w.id = :id
        """)
    Optional<WaitingEntity> findByIdWithJoins(@Param("id") Long id);

    boolean existsByReservationDateAndTimeIdAndThemeId(LocalDate reservationDate, long timeId, long themeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT w FROM WaitingEntity w
        JOIN FETCH w.time
        JOIN FETCH w.theme
        WHERE w.reservationDate = :date
          AND w.timeId = :timeId
          AND w.themeId = :themeId
        ORDER BY w.createdAt ASC, w.id ASC
        LIMIT 1
        """)
    Optional<WaitingEntity> findEarliestBySlotForUpdate(
        @Param("date") LocalDate date,
        @Param("timeId") long timeId,
        @Param("themeId") long themeId
    );

    @Query(nativeQuery = true, value = """
        WITH ranked AS (
            SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                   w.time_id, w.theme_id,
                   ROW_NUMBER() OVER (
                       PARTITION BY w.reservation_date, w.time_id, w.theme_id
                       ORDER BY w.created_at
                   ) AS rank
            FROM waiting w
        )
        SELECT r.id, r.customer_name AS customerName, r.reservation_date AS reservationDate,
               r.created_at AS createdAt, r.rank AS rank,
               t.id AS timeId, t.start_at AS timeStartAt,
               th.id AS themeId, th.name AS themeName, th.description AS themeDescription,
               th.thumbnail_url AS themeThumbnailUrl
        FROM ranked r
        JOIN reservation_time t ON r.time_id = t.id
        JOIN theme th ON r.theme_id = th.id
        ORDER BY r.reservation_date ASC, t.start_at ASC, r.rank ASC
        """)
    List<WaitingRankProjection> findAllWithRankProjection();

    @Query(nativeQuery = true, value = """
        WITH ranked AS (
            SELECT w.id, w.customer_name, w.reservation_date, w.created_at,
                   w.time_id, w.theme_id,
                   ROW_NUMBER() OVER (
                       PARTITION BY w.reservation_date, w.time_id, w.theme_id
                       ORDER BY w.created_at
                   ) AS rank
            FROM waiting w
        )
        SELECT r.id, r.customer_name AS customerName, r.reservation_date AS reservationDate,
               r.created_at AS createdAt, r.rank AS rank,
               t.id AS timeId, t.start_at AS timeStartAt,
               th.id AS themeId, th.name AS themeName, th.description AS themeDescription,
               th.thumbnail_url AS themeThumbnailUrl
        FROM ranked r
        JOIN reservation_time t ON r.time_id = t.id
        JOIN theme th ON r.theme_id = th.id
        WHERE r.customer_name = :customerName
          AND (r.reservation_date > :date OR (r.reservation_date = :date AND t.start_at > :time))
        ORDER BY r.reservation_date ASC
        """)
    List<WaitingRankProjection> findWithRankByCustomerNameAfter(
        @Param("customerName") String customerName,
        @Param("date") LocalDate date,
        @Param("time") String time
    );
}
