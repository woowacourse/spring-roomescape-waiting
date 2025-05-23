package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByThemeAndDate(Theme theme, LocalDate date);
    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);
    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);
    List<Reservation> findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByIdAsc(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme,
            ReservationStatus reservationStatus
    );
    @Query(value = """
    SELECT  new roomescape.reservation.WaitingRankReservation(r, 
        (
            SELECT COUNT(r2)
            FROM Reservation r2
            WHERE 
                r2.reservationStatus='WAITING' AND 
                r2.id <= r.id    AND 
                r2.reservationTime=r.reservationTime AND
                r2.theme=r.theme AND
                r2.date=r.date
        ))
    FROM Reservation r
    WHERE r.member = :member
    ORDER BY r.id ASC
    """)
    List<WaitingRankReservation> findAllByMember(Member member);

    boolean existsByReservationTime(ReservationTime reservationTime);
    boolean existsByTheme(Theme theme);
    boolean existsByDateAndReservationTimeAndThemeAndMember(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    );
    boolean existsByReservationTimeAndDateAndThemeAndReservationStatus(
            ReservationTime reservationTime, LocalDate date,
            Theme theme,
            ReservationStatus reservationStatus
    );
}
