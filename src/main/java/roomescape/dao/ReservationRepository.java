package roomescape.dao;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import roomescape.domain.MyReservation;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByStatus(ReservationStatus status, Pageable pageable);

    long countByStatus(ReservationStatus status);

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByDateAndTime_IdAndTheme_IdAndStatus(
            LocalDate date, Long timeId, Long themeId, ReservationStatus status);

    boolean existsByDateAndTime_IdAndTheme_IdAndNameAndStatus(
            LocalDate date, Long timeId, Long themeId, String name, ReservationStatus status);

    List<Reservation> findByNameAndStatus(String name, ReservationStatus status);

    Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id AND r.status = :status")
    Optional<Reservation> findByIdAndStatusForUpdate(@Param("id") Long id, @Param("status") ReservationStatus status);

    Optional<Reservation> findFirstByDateAndTime_IdAndTheme_IdAndStatusOrderByCreatedAtAscIdAsc(
            LocalDate date, Long timeId, Long themeId, ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.date = :date AND r.time.id = :timeId AND r.theme.id = :themeId AND r.status = :status ORDER BY r.createdAt ASC, r.id ASC")
    List<Reservation> findFirstWaitingForUpdate(
            @Param("date") LocalDate date, @Param("timeId") Long timeId,
            @Param("themeId") Long themeId, @Param("status") ReservationStatus status,
            Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reservation r SET r.status = :status WHERE r.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") ReservationStatus status);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Reservation r SET r.date = :date, r.time = :time WHERE r.id = :id")
    void updateDateAndTime(@Param("id") Long id, @Param("date") LocalDate date, @Param("time") ReservationTime time);

    @Query("""
            SELECT COUNT(r) FROM Reservation r
            WHERE r.date = :date AND r.time.id = :timeId AND r.theme.id = :themeId AND r.status = :status
            AND (r.createdAt < :createdAt OR (r.createdAt = :createdAt AND r.id < :id))
            """)
    long countWaitingBefore(
            @Param("date") LocalDate date, @Param("timeId") Long timeId, @Param("themeId") Long themeId,
            @Param("status") ReservationStatus status, @Param("createdAt") LocalDateTime createdAt,
            @Param("id") Long id);

    @Query("""
            SELECT new roomescape.domain.MyReservation(
                w,
                (SELECT COUNT(w2) + 1 FROM Reservation w2
                 WHERE w2.theme = w.theme
                   AND w2.date = w.date
                   AND w2.time = w.time
                   AND w2.status = :status
                   AND (w2.createdAt < w.createdAt
                        OR (w2.createdAt = w.createdAt AND w2.id < w.id))))
            FROM Reservation w
            WHERE w.name = :name AND w.status = :status
            """)
    List<MyReservation> findWaitingWithRankByName(
            @Param("name") String name, @Param("status") ReservationStatus status);

    @Query("""
            SELECT new roomescape.domain.MyReservation(
                w,
                (SELECT COUNT(w2) + 1 FROM Reservation w2
                 WHERE w2.theme = w.theme
                   AND w2.date = w.date
                   AND w2.time = w.time
                   AND w2.status = :status
                   AND (w2.createdAt < w.createdAt
                        OR (w2.createdAt = w.createdAt AND w2.id < w.id))))
            FROM Reservation w
            WHERE w.status = :status
            ORDER BY w.createdAt ASC, w.id ASC
            """)
    List<MyReservation> findAllWaitingWithRank(@Param("status") ReservationStatus status);
}
