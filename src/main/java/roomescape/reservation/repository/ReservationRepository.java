package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    default boolean existsBy(Long themeId) {
        return existsByThemeId(themeId);
    }

    default boolean existsBy(LocalDate date, Long timeId, Long themeId) {
        return existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    default boolean existsBy(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }


    @Query("""
            select exists
                (select r from Reservation r
                    where r.reservationDatetime.reservationDate.date = :date
                    and r.reservationDatetime.reservationTime.id = :timeId
                    and r.theme.id = :themeId
                    and r.reserver.id = :memberId)
            """)
    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    @Query("""
            select exists
                (select r from Reservation r
                    where r.reservationDatetime.reservationDate.date = :date
                    and r.reservationDatetime.reservationTime.id = :timeId
                    and r.theme.id = :themeId)
            """)
    boolean existsByDateAndTimeIdAndThemeId(
            LocalDate date,
            Long timeId,
            Long themeId
    );

    @Query("""
             select exists
                (select r from Reservation r
                where r.reservationDatetime.reservationTime.id = :timeId)
            """)
    boolean existsByTimeId(@Param(value = "timeId") Long timeId);

    @Query("""
            select exists
                (select r from Reservation r
                where r.theme.id = :themeId)
            """)
    boolean existsByThemeId(Long themeId);

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
            order by r.reservationDatetime.reservationDate.date asc, r.reservationDatetime.reservationTime.startAt asc
            """)
    List<Reservation> findByMemberId(@Param("memberId") Long memberId);

    @Query("""
            select r
            from Reservation r
            join fetch r.reservationDatetime.reservationTime t
            join fetch r.theme th
            where r.reservationDatetime.reservationDate.date = :date
              and t.id = :timeId
              and th = :theme
            """)
    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Theme theme);

    List<Reservation> findByStatus(ReservationStatus reservationStatus);
}
