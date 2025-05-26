package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndStatusStatus(
            LocalDate date,
            Long timeId,
            Long themeId,
            ReservationStatus status
    );

    List<Reservation> findByMember(Member member);

    List<Reservation> findByStatusStatus(ReservationStatus reservationStatus);

    @Query("""
            SELECT r
            FROM Reservation r 
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

    @Query("""
            SELECT COUNT(r) + 1
            FROM Reservation r
            JOIN r.status w
            WHERE r.theme = :theme
                AND r.date = :date
                AND r.time = :time
                AND w.status = roomescape.domain.ReservationStatus.WAITING
                AND w.savedDateTime < :myCreatedAt
            """)
    long countByReservationStatusOrderByCreatedAt(
            @Param("theme") Theme theme,
            @Param("date") LocalDate date,
            @Param("time") ReservationTime time,
            @Param("myCreatedAt") LocalDateTime myCreatedAt
    );
}
