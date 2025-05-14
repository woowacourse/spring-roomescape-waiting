package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findAll();

    boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme);

    List<Reservation> findByThemeAndMemberAndDateBetween(final Theme theme, final Member member,
                                                         final LocalDate dateFrom,
                                                         final LocalDate dateTo);

    @Query("""
                SELECT r.theme
                FROM Reservation r
                WHERE r.date >= :dateFrom AND r.date < :dateTo
                GROUP BY r.theme
                ORDER BY COUNT(r) DESC
            """)
    List<Theme> findTop10ThemesByReservationCountWithin7Days(final LocalDate dateFrom, final LocalDate dateTo,
                                                             final PageRequest pageRequest);

    List<Reservation> findByMember(final Member member);
}
