package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByThemeIdAndDate(Long themeId, LocalDate reservationDate);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN r.time t
        JOIN r.theme tm
        JOIN r.member m
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
        AND (:themeId IS NULL OR r.theme.id = :themeId)
        AND (:dateFrom IS NULL OR r.date >= :dateFrom)
        AND (:dateTo IS NULL OR r.date <= :dateTo)
        ORDER BY r.id
    """)
    List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo);
}
