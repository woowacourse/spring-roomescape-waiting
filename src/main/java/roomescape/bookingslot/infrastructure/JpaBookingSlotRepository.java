package roomescape.bookingslot.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface JpaBookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    @Query("""
            SELECT r
            FROM BookingSlot r
            JOIN FETCH r.time t
            JOIN FETCH r.theme th
            JOIN FETCH r.waitings w
            JOIN FETCH w.member m
            WHERE th.id = :themeId
              AND m.id = :memberId
              AND r.date BETWEEN :startDate AND :endDate
            """)
    List<BookingSlot> findByThemeIdAndDateBetweenAndWaitingsMemberId(Long themeId,
                                                                     LocalDate startDate, LocalDate endDate,
                                                                     Long memberId);

    @Query("SELECT EXISTS (SELECT 1 FROM BookingSlot r WHERE r.time.id = :timeId) ")
    boolean existsByTimeId(Long timeId);

    @Query("SELECT EXISTS (SELECT 1 FROM BookingSlot r WHERE r.theme.id = :themeId) ")
    boolean existsByThemeId(Long themeId);

    @Query("SELECT EXISTS (SELECT 1 FROM BookingSlot r WHERE (r.date, r.time.id, r.theme.id) = (:date, :timeId, :themeId)) ")
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT new roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse(rt.id, rt.startAt, 
            r.id IS NOT NULL) 
            FROM ReservationTime AS rt 
            LEFT JOIN BookingSlot r ON rt.id = r.time.id AND r.date = :date AND r.theme.id = :themeId 
            ORDER BY rt.startAt
            """)
    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
            SELECT r 
            FROM BookingSlot r                
            JOIN r.time t 
            JOIN r.theme th                   
            WHERE th.id = :themeId     
              AND t.id = :timeId
              AND r.date = :date
            """)
    Optional<BookingSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
