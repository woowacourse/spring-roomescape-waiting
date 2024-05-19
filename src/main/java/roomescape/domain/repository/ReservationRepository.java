package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findByMemberAndDateGreaterThanEqual(Member member, LocalDate date, Sort sort);

    //  TODO Eager loading을 하는데 Lazy로 설정할 필요?
    @Query("select r from Reservation r where r.date = :date and r.theme = :theme")
    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    // TODO nullable 사용보다 더 좋은 방법 고민해보기
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
