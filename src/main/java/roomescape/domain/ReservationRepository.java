package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r.time from Reservation r where r.date = :date and r.theme = :theme")
    List<ReservationTime> findTimeByDateAndTheme(LocalDate date, Theme theme);

    @Query("""
            select r.theme
            from Reservation r
            where r.date between :startDate and :endDate
            group by r.theme
            order by count(r) desc
            limit :limit""")
    List<Theme> findTopThemesDurationOrderByCount(LocalDate startDate, LocalDate endDate, Integer limit);

    @Query("""
            select r
            from Reservation r
            where r.date between :startDate and :endDate
                and r.theme = :theme
                and r.member = :member""")
    List<Reservation> findByDateBetweenAndThemeAndMember(LocalDate startDate, LocalDate endDate, Theme theme,
                                                             Member member);

    boolean existsByTime(ReservationTime time);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTheme(Theme theme);

    List<Reservation> findByMemberAndDateGreaterThanEqual(Member member, LocalDate date, Sort sort);
}
