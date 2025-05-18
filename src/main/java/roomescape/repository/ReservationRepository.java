package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    boolean existsByReservationTime(ReservationTime reservationTime);

    boolean existsByThemeAndDateAndReservationTime(Theme theme, LocalDate date, ReservationTime reservationTime);

    @Query(value =
            """
                    SELECT r
                    FROM Reservation AS r
                    INNER JOIN ReservationTime AS rt ON r.reservationTime.id = rt.id
                    INNER JOIN Theme AS t ON r.theme.id = t.id
                    INNER JOIN Users AS u ON r.user.id = u.id
                    WHERE u.id = :userId
                    AND t.id = :themeId
                    AND r.date BETWEEN :fromDate AND :toDate
                    ORDER BY r.id
                    """)
    List<Reservation> findReservationsByFilter(
            @Param("userId") Long userId,
            @Param("themeId") Long themeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
