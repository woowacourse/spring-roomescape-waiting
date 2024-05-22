package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
            SELECT r
            FROM Reservation AS r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.id = :id            
            """)
    Optional<Reservation> findByIdWithTimeAndTheme(@Param("id") Long id);

    @Query("""
            SELECT r
            FROM Reservation AS r
            WHERE r.waitingStatus.waitingNumber > 1 
            """)
    List<Reservation> findWaitings();

}
