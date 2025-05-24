package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    default Reservation getByIdOrThrow(final Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
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
    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(
            final Long themeId,
            final Long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    );

    @EntityGraph(attributePaths = {
            "reservationSlot",
            "reservationSlot.time",
            "reservationSlot.theme"
    })
    List<Reservation> findAllByMemberId(Long memberId);

    @Query("""
            SELECT
            (
                SELECT COUNT(r2)
                FROM Reservation r2
                WHERE r2.reservationSlot = r1.reservationSlot
                AND   r2.id <= r1.id
            )
            FROM Reservation r1
            WHERE r1.id = :reservationId
            """)
    Long getReservationRankById(Long reservationId);
}
