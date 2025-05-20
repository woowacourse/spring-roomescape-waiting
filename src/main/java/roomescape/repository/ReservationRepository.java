package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMember(Member member);

    boolean existsByReservationTime(ReservationTime reservationTime);

    boolean existsByTheme(Theme theme);

    boolean existsByThemeAndDateAndReservationTime(Theme theme, LocalDate date, ReservationTime reservationTime);

    @Query(value =
            """
                    SELECT r
                    FROM Reservation AS r
                    INNER JOIN ReservationTime AS rt ON r.reservationTime.id = rt.id
                    INNER JOIN Theme AS t ON r.theme.id = t.id
                    INNER JOIN Member AS m ON r.member.id = m.id
                    WHERE m.id = :userId
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
