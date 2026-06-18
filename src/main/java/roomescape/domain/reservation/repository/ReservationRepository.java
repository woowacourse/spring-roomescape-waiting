package roomescape.domain.reservation.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.error.type.ReservationErrorType;
import roomescape.domain.reservation.vo.ReservationSchedule;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;
import roomescape.global.error.exception.GeneralException;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByDeletedAtIsNullOrderByIdAsc();

    default List<ReservationWithWaitingNumber> findReservationsByNotDeletedWithWaitingNumber() {
        return toReservationsWithWaitingNumber(findAllByDeletedAtIsNullOrderByIdAsc());
    }

    default List<ReservationWithWaitingNumber> findReservationsByNameAndNotDeletedWithWaitingNumber(String name) {
        return toReservationsWithWaitingNumber(findAllByDeletedAtIsNullOrderByIdAsc())
            .stream()
            .filter(reservation -> reservation.reservation().getName().equals(name))
            .sorted(Comparator
                .comparing((ReservationWithWaitingNumber reservation) -> reservation.reservation().getDate())
                .thenComparing(reservation -> reservation.reservation().getTime().getStartAt())
                .thenComparing(reservation -> reservation.reservation().getId()))
            .toList();
    }

    private static List<ReservationWithWaitingNumber> toReservationsWithWaitingNumber(
        List<Reservation> reservations) {
        Map<ReservationSchedule, Integer> waitingCounts = new HashMap<>();

        return reservations.stream()
            .map(reservation -> {
                Integer waitingNumber = null;
                if (reservation.getStatus() == ReservationStatus.WAITING) {
                    ReservationSchedule schedule = reservation.getSchedule();
                    waitingNumber = waitingCounts.getOrDefault(schedule, 0) + 1;
                    waitingCounts.put(schedule, waitingNumber);
                }
                return new ReservationWithWaitingNumber(reservation, waitingNumber);
            })
            .toList();
    }

    @EntityGraph(attributePaths = {"time", "theme"})
    Optional<Reservation> findByIdAndDeletedAtIsNull(Long id);

    default Optional<Reservation> findReservationByIdAndNotDeleted(Long id) {
        return findByIdAndDeletedAtIsNull(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.time
        JOIN FETCH r.theme
        WHERE r.id = :id
          AND r.deletedAt IS NULL
        """)
    Optional<Reservation> lockReservationByIdAndNotDeleted(@Param("id") Long id);

    @Query("""
        SELECT r.time.id
        FROM Reservation r
        JOIN r.time rt
        JOIN r.theme t
        WHERE r.date = :date
          AND t.id = :themeId
          AND r.status = roomescape.domain.reservation.entity.ReservationStatus.ACTIVE
          AND r.deletedAt IS NULL
          AND rt.deletedAt IS NULL
          AND t.deletedAt IS NULL
        """)
    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(
        @Param("date") LocalDate localDate,
        @Param("themeId") Long themeId
    );

    default boolean existsActiveReservationBySchedule(ReservationSchedule schedule) {
        return existsActiveReservationBySchedule(schedule.date(), schedule.themeId(), schedule.timeId());
    }

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        JOIN r.time rt
        JOIN r.theme t
        WHERE r.date = :date
          AND t.id = :themeId
          AND rt.id = :timeId
          AND r.status = roomescape.domain.reservation.entity.ReservationStatus.ACTIVE
          AND r.deletedAt IS NULL
          AND rt.deletedAt IS NULL
          AND t.deletedAt IS NULL
        """)
    boolean existsActiveReservationBySchedule(
        @Param("date") LocalDate date,
        @Param("themeId") Long themeId,
        @Param("timeId") Long timeId
    );

    default Optional<Long> lockActiveReservationBySchedule(ReservationSchedule schedule) {
        return lockActiveReservationBySchedule(schedule.date(), schedule.themeId(), schedule.timeId())
            .map(Reservation::getId);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN r.time rt
        JOIN r.theme t
        WHERE r.date = :date
          AND t.id = :themeId
          AND rt.id = :timeId
          AND r.status = roomescape.domain.reservation.entity.ReservationStatus.ACTIVE
          AND r.deletedAt IS NULL
          AND rt.deletedAt IS NULL
          AND t.deletedAt IS NULL
        """)
    Optional<Reservation> lockActiveReservationBySchedule(
        @Param("date") LocalDate date,
        @Param("themeId") Long themeId,
        @Param("timeId") Long timeId
    );

    default Optional<Reservation> lockFirstWaitingReservationBySchedule(ReservationSchedule schedule) {
        return lockWaitingReservationsBySchedule(
                schedule.date(), schedule.themeId(), schedule.timeId(), PageRequest.of(0, 1))
            .stream()
            .findFirst();
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.time rt
        JOIN FETCH r.theme t
        WHERE r.date = :date
          AND t.id = :themeId
          AND rt.id = :timeId
          AND r.status = roomescape.domain.reservation.entity.ReservationStatus.WAITING
          AND r.deletedAt IS NULL
          AND rt.deletedAt IS NULL
          AND t.deletedAt IS NULL
        ORDER BY r.id ASC
        """)
    List<Reservation> lockWaitingReservationsBySchedule(
        @Param("date") LocalDate date,
        @Param("themeId") Long themeId,
        @Param("timeId") Long timeId,
        Pageable pageable
    );

    default Reservation update(Reservation reservation) {
        int updatedRowCount = updateByIdAndVersion(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getStatus(),
            reservation.getVersion()
        );

        if (updatedRowCount == 0) {
            if (!existsReservationByIdAndNotDeleted(reservation.getId())) {
                throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
            }
            if (existsByIdAndStatusAndDeletedAtIsNull(reservation.getId(), ReservationStatus.CANCELED)) {
                throw new GeneralException(ReservationErrorType.ALREADY_CANCELED);
            }
            throw new GeneralException(ReservationErrorType.RESERVATION_ALREADY_UPDATED);
        }

        return Reservation.reconstruct(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getStatus(),
            reservation.getVersion() + 1
        );
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Reservation r
        SET r.name = :name,
            r.date = :date,
            r.time = :time,
            r.theme = :theme,
            r.status = :status,
            r.version = r.version + 1
        WHERE r.id = :id
          AND r.deletedAt IS NULL
          AND r.status <> roomescape.domain.reservation.entity.ReservationStatus.CANCELED
          AND r.version = :version
        """)
    int updateByIdAndVersion(
        @Param("id") Long id,
        @Param("name") String name,
        @Param("date") LocalDate date,
        @Param("time") Time time,
        @Param("theme") Theme theme,
        @Param("status") ReservationStatus status,
        @Param("version") Long version
    );

    default void deleteReservationById(Long id) {
        int updatedRowCount = softDeleteById(id);
        if (updatedRowCount == 0) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND);
        }
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Reservation r
        SET r.deletedAt = CURRENT_TIMESTAMP
        WHERE r.id = :id
          AND r.deletedAt IS NULL
        """)
    int softDeleteById(@Param("id") Long id);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    default boolean existsReservationByIdAndNotDeleted(Long id) {
        return existsByIdAndDeletedAtIsNull(id);
    }

    boolean existsByIdAndStatusAndDeletedAtIsNull(Long id, ReservationStatus status);

    default boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status) {
        return existsByDateAndNameAndTimeIdAndThemeIdAndStatusAndDeletedAtIsNull(
            reservation.getDate(),
            reservation.getName(),
            reservation.getTime().getId(),
            reservation.getTheme().getId(),
            status
        );
    }

    boolean existsByDateAndNameAndTimeIdAndThemeIdAndStatusAndDeletedAtIsNull(
        LocalDate date,
        String name,
        Long timeId,
        Long themeId,
        ReservationStatus status
    );
}
