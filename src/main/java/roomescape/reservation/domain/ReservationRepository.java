package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r
            FROM Reservation AS r
            WHERE r.member.id = :memberId
            AND r.theme.id = :themeId
            AND r.date.value BETWEEN :dateFrom AND :dateTo
            """)
    List<Reservation> findByMemberAndThemeAndPeriod(@Param("memberId") Long memberId,
                                                    @Param("themeId") Long themeId,
                                                    @Param("dateFrom") LocalDate dateFrom,
                                                    @Param("dateTo") LocalDate dateTo);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findByMemberId(Long memberId);

    @Query("""
            SELECT COUNT(r)
            FROM Reservation AS r
            WHERE r.date.value = :date
            AND r.time.id = :timeId
            AND r.theme.id = :themeId        
            """)
    int countByDateAndTimeAndTheme(@Param("date") LocalDate date,
                                   @Param("timeId") Long timeId,
                                   @Param("themeId") Long themeId);
}
