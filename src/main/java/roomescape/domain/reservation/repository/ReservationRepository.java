package roomescape.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query(
            value = """
                    select
                        th.id as theme_id,
                        th.name as theme_name,
                        th.description as theme_description,
                        th.thumbnail as theme_thumbnail,
                        count(th.id) as reservation_count
                    from reservation as r
                    inner join theme as th
                    on r.theme_id = th.id
                    where r.date between dateadd('day', -7, current_date()) and dateadd('day', -1, current_date())
                    group by th.id
                    order by reservation_count desc
                    limit 10
                    """, nativeQuery = true
    )
    List<Theme> findThemeOrderByReservationCount();
}
