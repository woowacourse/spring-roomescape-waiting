package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRank;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByMember(Member member);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    boolean existsByTheme(Theme theme);

    boolean existsByTime(TimeSlot timeSlot);

    boolean existsByMemberAndDateAndTimeAndTheme(Member member, LocalDate date, TimeSlot timeSlot, Theme theme);

    @Query("""
            SELECT new roomescape.domain.ReservationRank(
            r,
            (SELECT COUNT(r2)
            FROM Reservation r2
            WHERE r2.theme = r.theme
                AND r2.date = r.date
                AND r2.time = r.time
                AND r2.id < r.id))
            FROM Reservation r
            WHERE r.member = :member
            ORDER BY r.date, r.time.startAt, r.id
            """)
    List<ReservationRank> findReservationRanksWithMember(Member member);
}
