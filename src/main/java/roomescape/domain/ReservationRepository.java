package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findByMemberAndDateGreaterThanEqual(Member member, LocalDate date, Sort sort);

    @Query("select r.time from Reservation r where r.date = :date and r.theme = :theme")
    List<ReservationTime> findTimeByDateAndTheme(LocalDate date, Theme theme);

    @Query("""
            select r.theme
            from Reservation r
            where r.date between :startDate and :endDate
            group by r.theme
            order by count(r) desc
            """)
    List<Theme> findTopThemesDurationOrderByCount(LocalDate startDate, LocalDate endDate, Limit limit);

    @Query("""
            select r
            from Reservation r
            join fetch r.theme
            join fetch r.time
            join fetch r.member
            where (:startDate is null or r.date >= :startDate)
                and (:endDate is null or r.date <= :endDate)
                and (:themeId is null or r.theme.id = :themeId)
                and (:memberId is null or r.member.id = :memberId)""")
    List<Reservation> findByConditions(@Nullable LocalDate startDate, @Nullable LocalDate endDate, @Nullable Long themeId,
                                       @Nullable Long memberId);

    boolean existsByTime(ReservationTime time);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTheme(Theme theme);
}
