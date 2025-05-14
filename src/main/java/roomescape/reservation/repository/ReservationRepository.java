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

    // TODO EXISTS를 사용해서 최적화하기

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
            where r.reservationDatetime.reservationDate.date = :date and r.theme.id = :themeId""")
    List<Long> findReservedTimeIdsByDateAndTheme(@Param(value = "date") LocalDate date,
                                                 @Param(value = "themeId") Long themeId);

    @Query("select count(*) from Reservation r")
    List<Reservation> findFilteredReservations(Long themeId, Long memberId, LocalDate from, LocalDate to);
}
