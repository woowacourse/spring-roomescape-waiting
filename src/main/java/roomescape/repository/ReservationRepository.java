package roomescape.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaitingWithRank;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(long memberId,
                                                             long themeId,
                                                             LocalDate dateFrom,
                                                             LocalDate dateTo);

    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date,
                                                       long timeId,
                                                       long themeId);

    boolean existsByReservationTimeId(long timeId);

    @Query("""
            SELECT CASE 
            WHEN COUNT(r) > 0 
            THEN true 
            ELSE false END 
            FROM Reservation r 
            WHERE r.member = :member 
            AND r.theme = :theme 
            AND r.reservationTime = :reservationTime 
            AND r.date = :date
            """)
    boolean hasBookedReservation(@Param("member") Member member,
                                 @Param("theme") Theme theme,
                                 @Param("reservationTime") ReservationTime reservationTime,
                                 @Param("date") LocalDate date);

    @Query("""
            SELECT new roomescape.domain.ReservationWaitingWithRank(
             r, (SELECT COUNT(r2) + 1 
                FROM Reservation r2 
                WHERE r2.theme = r.theme 
                AND r2.date = r.date 
                AND r2.reservationTime = r.reservationTime
                AND r2.reservationStatus = r.reservationStatus  
                AND r2.createdAt < r.createdAt)
             )
             FROM Reservation r 
             WHERE r.member.id = :memberId
             """)
    List<ReservationWaitingWithRank> findReservationWaitingWithRankByMemberId(@Param("memberId") long memberId);

    List<Reservation> findByReservationStatus(ReservationStatus reservationStatus);

    @Query("""
            SELECT r 
            FROM Reservation r 
            WHERE r.theme = :theme 
            AND r.reservationTime = :reservationTime 
            AND r.date = :date  
            AND r.reservationStatus = 'WAITING' 
            ORDER BY r.createdAt
            """)
    Optional<Reservation> findNextWaiting(@Param("theme") Theme theme,
                                          @Param("reservationTime") ReservationTime reservationTime,
                                          @Param("date") LocalDate date,
                                          Limit limit);
}

