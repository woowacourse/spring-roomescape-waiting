package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, Long timeId, Long themeId,
                                                     ReservationStatus status);

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

    List<Reservation> findAllByStatus(ReservationStatus status);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(LocalDate date, Long timeId, Long themeId,
                                                                Long memberId, ReservationStatus status);
}
