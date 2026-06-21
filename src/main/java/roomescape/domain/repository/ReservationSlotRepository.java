package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.dto.AdminReservationResponse;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long>, ReservationSlotRepositoryCustom {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT rs
            FROM ReservationSlot rs
            JOIN FETCH rs.time
            JOIN FETCH rs.theme
            WHERE rs.id = :id
            """)
    Optional<ReservationSlot> findByIdForUpdate(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT DISTINCT rs
        FROM ReservationSlot rs
        JOIN FETCH rs.time
        JOIN FETCH rs.theme
        LEFT JOIN FETCH rs.reservations
        WHERE rs.id = (
            SELECT slot.id
            FROM ReservationSlot slot
            JOIN slot.reservations reservation
            WHERE reservation.id = :reservationId
        )
        """)
    Optional<ReservationSlot> findByReservationIdForUpdate(@Param("reservationId") long reservationId);

    @Query("""
        SELECT rs.id
        FROM ReservationSlot rs
        JOIN rs.reservations reservation
        WHERE reservation.id = :reservationId
        """)
    Optional<Long> findSlotIdByReservationId(@Param("reservationId") long reservationId);

    @Query("""
            SELECT rs.id
            FROM ReservationSlot rs
            WHERE rs.date = :date
              AND rs.time.id = :timeId
              AND rs.theme.id = :themeId
            """)
    Optional<Long> findIdByDateAndTimeIdAndThemeId(
            @Param("date") LocalDate date,
            @Param("timeId") long timeId,
            @Param("themeId") long themeId
    );
}
