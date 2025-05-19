package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT DISTINCT r
            FROM Reservation r
            LEFT JOIN FETCH r.theme
            LEFT JOIN FETCH r.time
            WHERE r.member = :member
            """)
    List<Reservation> findByMember(Member member);

    @Query("""
            SELECT DISTINCT r
            FROM Reservation r
            LEFT JOIN FETCH r.member
            LEFT JOIN FETCH r.theme
            LEFT JOIN FETCH r.time
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:memberId IS NULL OR r.member.id = :memberId)
                AND (:dateFrom IS NULL OR r.date >= :dateFrom)
                AND (:dateTo IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findByMemberAndThemeAndDateRange(
            @Param("memberId") Long member,
            @Param("themeId") Long theme,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );
}
