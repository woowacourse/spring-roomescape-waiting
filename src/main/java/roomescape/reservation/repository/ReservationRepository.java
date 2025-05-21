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
                where r.reserver.id = :memberId
                and r.reservationDatetime.reservationDate.date = :date
                and r.reservationDatetime.reservationTime.id = :timeId
            )
            """)
    boolean existsByMemberIdAndDateAndTimeId(
            @Param(value = "memberId") Long memberId,
            @Param(value = "date") LocalDate date,
            @Param(value = "timeId") Long timeId
    );
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

    @Query("""
                select r from Reservation r
                join fetch r.reservationDatetime.reservationTime t
                join fetch r.theme th
                join fetch r.reserver m
                where (:themeId is null or r.theme.id = :themeId)
                  and (:memberId is null or r.reserver.id = :memberId)
                  and (:fromDate is null or r.reservationDatetime.reservationDate.date >= :fromDate)
                  and (:toDate is null or r.reservationDatetime.reservationDate.date <= :toDate)
            """)
    List<Reservation> findFilteredReservations(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            select r
            from Reservation r
            join fetch r.reserver
            join fetch r.reservationDatetime.reservationTime
            join fetch r.theme
            where r.reserver.id = :memberId
            """)
    List<Reservation> findByMemberId(@Param("memberId") Long memberId);
}
