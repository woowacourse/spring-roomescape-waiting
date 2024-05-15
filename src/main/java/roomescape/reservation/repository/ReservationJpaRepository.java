package roomescape.reservation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ReservationJpaRepository extends CrudRepository<Reservation, Long> {
    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findByThemeAndMember(Theme theme, Member member);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    @Query(value = """
            SELECT r.theme
            FROM Reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme
            ORDER BY COUNT(r.theme) DESC
            """)
    List<Theme> findTrendingThemesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}
