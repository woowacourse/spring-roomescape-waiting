package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE (:memberId IS NULL OR r.member.id = :memberId)
              AND (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:from IS NULL OR r.date >= :from)
              AND (:to IS NULL OR r.date <= :to)
            """)
    Collection<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    boolean existsByDateAndTimeId(LocalDate reservationDate, Long id);

    Collection<Reservation> findAllByMemberId(Long memberId);
}
