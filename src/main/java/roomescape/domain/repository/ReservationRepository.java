package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long id);

    @Query("""
            SELECT DISTINCT r
            FROM Reservation r
            LEFT JOIN FETCH r.gameSchedule gs
            LEFT JOIN FETCH gs.theme
            LEFT JOIN FETCH gs.time
            WHERE r.member = :member
            """)
    List<Reservation> findByMember(Member member);

    @Query("""
            SELECT DISTINCT r
            FROM Reservation r
            LEFT JOIN FETCH r.member
            LEFT JOIN FETCH r.gameSchedule gs
            LEFT JOIN FETCH gs.theme
            LEFT JOIN FETCH gs.time
            WHERE (:themeId IS NULL OR gs.theme.id = :themeId)
                AND (:memberId IS NULL OR r.member.id = :memberId)
                AND (:dateFrom IS NULL OR gs.date >= :dateFrom)
                AND (:dateTo IS NULL OR gs.date <= :dateTo)
            """)
    List<Reservation> findByMemberAndThemeAndDateRange(
            @Param("memberId") Long member,
            @Param("themeId") Long theme,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    boolean existsByGameScheduleId(Long id);
}
