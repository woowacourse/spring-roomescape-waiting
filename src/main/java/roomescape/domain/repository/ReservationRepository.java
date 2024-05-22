package roomescape.domain.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findByMemberAndDateGreaterThanEqual(Member member, LocalDate date, Sort sort);

    @Query("select r from Reservation r where r.date = :date and r.theme = :theme")
    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

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
    List<Reservation> findByConditions(
            Optional<LocalDate> startDate,
            Optional<LocalDate> endDate,
            Long themeId,
            Long memberId
    );

    List<Reservation> findByMember(Member member);

    boolean existsByTime(ReservationTime time);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTheme(Theme theme);
}
