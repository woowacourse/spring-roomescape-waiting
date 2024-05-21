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
    List<Reservation> findByMember_Id(Long memberId);

    int countByDateValueAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);
}
