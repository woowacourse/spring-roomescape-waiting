package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Pageable FIRST_RESULT = PageRequest.of(0, 1);

    default Reservation getByIdOrThrow(final Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    Boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.time t
                JOIN FETCH r.theme th
                JOIN FETCH r.member m
                WHERE th.id = :themeId
                  AND m.id = :memberId
                  AND r.date BETWEEN :dateFrom AND :dateTo
            """)
    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(
            final Long themeId,
            final Long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    );

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    @Query("""
            SELECT
            (
                SELECT COUNT(r2)
                FROM Reservation r2
                WHERE r2.date = r1.date
                AND   r2.theme = r1.theme
                AND   r2.time = r1.time
                AND   r2.id <= r1.id
            )
            FROM Reservation r1
            WHERE r1.id = :reservationId
            """)
    Long getReservationRankByReservationId(Long reservationId);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.date = :date
            AND r.time.id = :timeId
            AND r.theme.id = :themeId
            AND r.status = 'WAITING'
            ORDER BY r.id ASC
            """)
    List<Reservation> findWaitingReservations(LocalDate date, Long timeId, Long themeId, Pageable pageable);

    default Optional<Reservation> findFirstRankWaitingBy(LocalDate date, Long timeId, Long themeId) {
        List<Reservation> result = findWaitingReservations(date, timeId, themeId, FIRST_RESULT);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.getFirst());
    }

    List<Reservation> findAllByStatus(BookingStatus bookingStatus);
}
