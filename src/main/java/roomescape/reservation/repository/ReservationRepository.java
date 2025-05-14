package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select exists
                (select r from Reservation r
                where r.reservationDatetime.reservationDate.date = :date and r.reservationDatetime.reservationTime.id = :timeId)
            """)
    boolean existsByDateAndTimeId(@Param(value = "date") LocalDate date, @Param(value = "timeId") Long timeId);

    @Query("""
             select exists
                (select r from Reservation r
                where r.reservationDatetime.reservationTime.id = :timeId)
            """)
    boolean existsByTimeId(@Param(value = "timeId") Long timeId);

    boolean existsByTheme_Id(Long themeId);

    @Query("""
            select count(*) from Reservation r
            where r.theme.id = :themeId
            and r.reservationDatetime.reservationDate.date between :from and :to
            """)
    long countReservationByThemeIdAndDuration(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("themeId") Long themeId
    );

    @Query("""
            select r.reservationDatetime.reservationTime.id
            from Reservation r
            where r.reservationDatetime.reservationDate.date = :date and r.theme.id = :themeId
            """)
    List<Long> findReservedTimeIdsByDateAndTheme(@Param(value = "date") LocalDate date,
                                                 @Param(value = "themeId") Long themeId);

    // TODO 페치를 사용하는 것으로 인한 성능 저하

    @Query("""
                SELECT r FROM Reservation r
                JOIN fetch r.reservationDatetime.reservationTime t
                JOIN fetch r.theme th
                JOIN fetch r.reserver m
                WHERE (:themeId IS NULL OR r.theme.id = :themeId)
                  AND (:memberId IS NULL OR r.reserver.id = :memberId)
                  AND (:fromDate IS NULL OR r.reservationDatetime.reservationDate.date >= :fromDate)
                  AND (:toDate IS NULL OR r.reservationDatetime.reservationDate.date <= :toDate)
            """)
    List<Reservation> findFilteredReservations(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.reserver
            JOIN FETCH r.reservationDatetime.reservationTime
            JOIN FETCH r.theme
            WHERE r.reserver.id = :memberId
            """)
    List<Reservation> findByMemberId(@Param("memberId") Long memberId);
}
