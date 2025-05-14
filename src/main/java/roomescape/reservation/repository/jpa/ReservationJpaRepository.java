package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findAllByMemberId(Long id);

    @Query("""
        SELECT r 
        FROM Reservation r
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
        AND (:themeId IS NULL OR r.theme.id = :themeId)
        AND (:from <= r.date AND :to >= r.date)
    """)
    List<Reservation> findAllByCondition(@Param("memberId") Long memberId, @Param("themeId") Long themeId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
