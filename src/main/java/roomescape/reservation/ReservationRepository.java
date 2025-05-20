package roomescape.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByThemeAndDate(Theme theme, LocalDate date);

    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);

    List<Reservation> findAllByMember(Member member);

    List<Reservation> findAllByDateAndReservationTimeAndThemeAndReservationStatus(LocalDate date, ReservationTime reservationTime, Theme theme, ReservationStatus reservationStatus);

    Boolean existsByReservationTime(ReservationTime reservationTime);

    Boolean existsByTheme(Theme theme);

    Boolean existsByReservationTimeAndDateAndTheme(ReservationTime reservationTime, LocalDate date, Theme theme);
}
