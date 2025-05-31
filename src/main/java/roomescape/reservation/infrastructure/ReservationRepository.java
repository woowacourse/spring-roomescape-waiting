package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Pageable FIRST_RESULT = PageRequest.of(0, 1);

    default Reservation getByIdOrThrow(final Long id) {
        return this.findById(id).orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    Boolean existsByReservationSlotAndMemberId(ReservationSlot reservationSlot, Long memberId);

    @Query("""
                SELECT r, rs
                FROM Reservation r
                JOIN FETCH r.reservationSlot rs
                JOIN FETCH rs.theme
                JOIN FETCH rs.time
                JOIN FETCH r.member
                WHERE rs.theme.id = :themeId
                  AND r.member.id = :memberId
                  AND rs.date BETWEEN :dateFrom AND :dateTo
            """)
    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(final Long themeId, final Long memberId,
                                                              final LocalDate dateFrom, final LocalDate dateTo);

    @EntityGraph(attributePaths = {"reservationSlot", "reservationSlot.time", "reservationSlot.theme"})
    List<Reservation> findAllByMemberId(Long memberId);

    @Query("""
            SELECT
            (
                SELECT COUNT(r2)
                FROM Reservation r2
                WHERE r2.reservationSlot.id = r1.reservationSlot.id
                AND   r2.id <= r1.id
                AND   r2.status = 'WAITING'
            )
            FROM Reservation r1
            WHERE r1.id = :reservationId
            """)
    Long getReservationRankById(Long reservationId);

    @EntityGraph(attributePaths = {"reservationSlot", "reservationSlot.time", "reservationSlot.theme",})
    List<Reservation> findByStatus(BookingStatus status);

    @EntityGraph(attributePaths = {"reservationSlot", "reservationSlot.time", "reservationSlot.theme"})
    List<Reservation> findAll();

    Boolean existsByReservationSlotIdAndStatus(Long reservationSlotId, BookingStatus status);

    default Boolean existsConfirmedReservation(final Long reservationSlotId) {
        return existsByReservationSlotIdAndStatus(reservationSlotId, BookingStatus.RESERVED);
    }

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.reservationSlot.id = :reservationSlotId
            AND r.status = 'WAITING'
            ORDER BY r.id ASC
            """)
    List<Reservation> findRankWaitingReservations(Long reservationSlotId, Pageable pageable);

    default Optional<Reservation> findFirstRankWaitingBy(final Long reservationSlotId) {
        final List<Reservation> result = findRankWaitingReservations(reservationSlotId, FIRST_RESULT);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getFirst());
    }
}
