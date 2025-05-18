package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    default Reservation getByIdOrThrow(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

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
}
