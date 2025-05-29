package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>, ReservationRepository {
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
    boolean existsByTimeId(long timeId);
    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);
    boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId, long memberId);
    @Query("""
            SELECT r FROM Reservation r
            WHERE (:memberId IS NULL OR r.member.id = :memberId)
                AND (:themeId IS NULL OR r.theme.id = :themeId)
                AND (r.date >= :from AND r.date <= :to)
            """)
    List<Reservation> findAllByCondition(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
