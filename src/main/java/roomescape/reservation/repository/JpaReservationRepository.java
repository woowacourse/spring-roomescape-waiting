package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r 
            FROM Reservation r                
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            JOIN FETCH r.member m                   
            WHERE th.id = :themeId     
              AND m.id = :memberId    
              AND r.date BETWEEN :startDate AND :endDate 
            """)
    List<Reservation> findFilteredReservations(Long themeId,
                                               Long memberId,
                                               LocalDate startDate,
                                               LocalDate endDate);

    @Query("SELECT EXISTS (SELECT 1 FROM Reservation r WHERE r.time.id = :timeId) ")
    boolean existsByTimeId(Long timeId);

    @Query("SELECT EXISTS (SELECT 1 FROM Reservation r WHERE r.theme.id = :themeId) ")
    boolean existsByThemeId(Long themeId);

    @Query("SELECT EXISTS (SELECT 1 FROM Reservation r WHERE (r.date, r.time.id, r.theme.id) = (:date, :timeId, :themeId)) ")
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT new roomescape.reservationtime.dto.response.AvailableReservationTimeResponse(rt.id, rt.startAt, 
            r.id IS NOT NULL) 
            FROM ReservationTime AS rt 
            LEFT JOIN Reservation r ON rt.id = r.time.id AND r.date = :date AND r.theme.id = :themeId 
            ORDER BY rt.startAt
            """)
    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
            SELECT  r 
            FROM Reservation r 
            JOIN FETCH r.member m 
            WHERE m.id = :memberId 
            """
    )
    List<Reservation> findByMemberId(Long memberId);
}
