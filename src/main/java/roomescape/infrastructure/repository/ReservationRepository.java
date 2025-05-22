package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTime(ReservationTime time);

    boolean existsByTheme(Theme theme);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.time
        WHERE r.theme.id =:themeId
        AND r.member.id =:memberId
        AND (r.date BETWEEN :dateFrom AND :dateTo)
    """)
    List<Reservation> findAllByThemeAndMemberAndDate(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findAllByMember(Member member);

    Reservation findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);
}
