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
                where r.reservationDateTime.reservationDate.date = :date and r.reservationDateTime.reservationTime.id = :timeId)
            """)
    boolean existsByDateAndTimeId(@Param("date") LocalDate date, @Param("timeId") Long timeId);

    @Query("""
             select exists
                (select r from Reservation r
                where r.reservationDateTime.reservationTime.id = :timeId)
            """)
    boolean existsByTimeId(@Param("timeId") Long timeId);

    boolean existsByTheme_Id(Long themeId);

    @Query("""
            select count(*) from Reservation r
            where r.theme.id = :themeId
            and r.reservationDateTime.reservationDate.date between :from and :to
            """)
    long countReservationByThemeIdAndDuration(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("themeId") Long themeId
    );

    @Query("""
            select r.reservationDateTime.reservationTime.id
            from Reservation r
            where r.reservationDateTime.reservationDate.date = :date and r.theme.id = :themeId
            """)
    List<Long> findReservedTimeIdsByDateAndTheme(@Param("date") LocalDate date,
                                                 @Param("themeId") Long themeId);
}
