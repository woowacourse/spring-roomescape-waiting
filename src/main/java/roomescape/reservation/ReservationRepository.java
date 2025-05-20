package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByThemeAndDate(Theme theme, LocalDate date);
    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);
    List<Reservation> findAllByMember(Member member);

    boolean existsByReservationTime(ReservationTime reservationTime);
    boolean existsByTheme(Theme theme);
    boolean existsByReservationTimeAndDateAndTheme(ReservationTime reservationTime, LocalDate date, Theme theme);
}
