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
            SELECT r
            FROM Reservation AS r
            JOIN FETCH r.member 
            WHERE r.date.value = :date
            AND r.time.id = :timeId
            AND r.theme.id = :themeId        
            """)
    List<Reservation> findByDateAndTimeAndTheme(@Param("date") LocalDate date,
                                                @Param("timeId") Long timeId,
                                                @Param("themeId") Long themeId);

    @Query("""
            SELECT r1
            FROM Reservation AS r1
            WHERE EXISTS (
                SELECT r2
                FROM Reservation AS r2
                WHERE r2.date = r1.date
                AND r2.theme = r1.theme
                AND r2.time = r1.time
                AND r1.id > r2.id
            )
            """)
    List<Reservation> findWaitings();
}
