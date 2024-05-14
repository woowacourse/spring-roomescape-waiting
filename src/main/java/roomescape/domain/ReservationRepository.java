package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
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

    @Query("select case when(count(r) > 0) then true else false end from Reservation r where r.time = :time")
    boolean existByTime(ReservationTime time);

    @Query("""
            select case when(count(r) > 0) then true else false end
            from Reservation r
            where r.time = :time
                    and r.date = :date
                    and r.theme = :theme""")
    boolean existByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);


    @Query("select case when(count(r) > 0) then true else false end from Reservation r where r.theme = :theme")
    boolean existByTheme(Theme theme);

}
