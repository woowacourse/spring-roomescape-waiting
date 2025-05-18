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

    Boolean existsByReservationTime(ReservationTime reservationTime);
    Boolean existsByTheme(Theme theme);
    Boolean existsByReservationTimeAndDateAndTheme(ReservationTime reservationTime, LocalDate date, Theme theme);
}
