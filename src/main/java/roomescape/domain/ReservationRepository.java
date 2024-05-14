package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r.time from Reservation r where r.date = :date and r.theme.id = :themeId")
    List<ReservationTime> findTimeByDateAndThemeId(LocalDate date, Long themeId);

    //TODO existByTime 고려해보기
    @Query("select case when(count(*)>0) then true else false end from Reservation r where r.time.id = :timeId")
    boolean existByTimeId(Long timeId);

    @Query("""
            select case when(count(*)>0) then true else false end
            from Reservation r
            where r.time.id = :timeId
                    and r.date = :date
                    and r.theme.id = :themeId""")
    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("select case when(count(*)>0) then true else false end from Reservation r where r.theme.id = :themeId")
    boolean existByThemeId(Long themeId);

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
                and r.theme.id = :themeId
                and r.member.id = :memberId""")
    List<Reservation> findByDateBetweenAndThemeIdAndMemberId(LocalDate startDate, LocalDate endDate, Long themeId,
                                                             Long memberId);
}
